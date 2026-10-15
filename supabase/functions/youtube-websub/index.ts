// Supabase Edge Function for YouTube WebSub (PubSubHubbub) Real-Time Push Webhooks
// Public URL: https://eiomktvavndorazreyba.supabase.co/functions/v1/youtube-websub
import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const supabaseUrl = Deno.env.get("SUPABASE_URL") || "";
const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") || "";
const supabaseAdmin = createClient(supabaseUrl, supabaseServiceKey);

// Fast regex extractor for Google Atom XML webhook payloads
function extractXmlTag(xml: string, tag: string): string | null {
  const match = xml.match(new RegExp(`<${tag}[^>]*>([\\s\\S]*?)<\\/${tag}>`, "i"));
  return match ? match[1].trim() : null;
}

serve(async (req: Request) => {
  const url = new URL(req.url);

  // 1. Verification Handshake (GET request from Google PubSubHubbub Hub)
  if (req.method === "GET") {
    const hubMode = url.searchParams.get("hub.mode");
    const hubChallenge = url.searchParams.get("hub.challenge");
    const hubTopic = url.searchParams.get("hub.topic");

    if (hubChallenge && (hubMode === "subscribe" || hubMode === "unsubscribe")) {
      console.log(`[WebSub Handshake] Confirmed ${hubMode} for topic: ${hubTopic}`);
      // Google requires plain text response with the challenge code and HTTP 200
      return new Response(hubChallenge, {
        status: 200,
        headers: { "Content-Type": "text/plain" },
      });
    }

    return new Response("Missing hub.challenge", { status: 400 });
  }

  // 2. Video Upload Notification (POST request from Google Hub)
  if (req.method === "POST") {
    try {
      const xml = await req.text();
      if (!xml || !xml.includes("<entry>")) {
        // Empty or ping request
        return new Response("OK", { status: 200 });
      }

      const videoId = extractXmlTag(xml, "yt:videoId") || extractXmlTag(xml, "videoId");
      const channelId = extractXmlTag(xml, "yt:channelId") || extractXmlTag(xml, "channelId");
      const rawTitle = extractXmlTag(xml, "title") || "";
      const published = extractXmlTag(xml, "published") || "";

      // Decode common XML entities in title
      const title = rawTitle
        .replace(/&amp;/g, "&")
        .replace(/&lt;/g, "<")
        .replace(/&gt;/g, ">")
        .replace(/&quot;/g, '"')
        .replace(/&#39;/g, "'");

      console.log(`[WebSub Push] Received video ${videoId} from channel ${channelId}: "${title}"`);

      if (!videoId || !title) {
        return new Response("Missing videoId or title", { status: 200 });
      }

      // Quick filter: Check for obvious non-highlights
      const lower = title.toLowerCase();
      const isForbidden =
        lower.includes("press conference") ||
        lower.includes("conférence") ||
        lower.includes("point presse") ||
        lower.includes("interview") ||
        lower.includes("training") ||
        lower.includes("#shorts") ||
        lower.includes("live stream") ||
        lower.includes("retro") ||
        lower.includes("classic match");

      if (isForbidden) {
        console.log(`[WebSub Push] Skipped non-highlight video: "${title}"`);
        return new Response("Filtered non-highlight", { status: 200 });
      }

      // Find candidates among recently FINISHED matches in last 72 hours without highlights
      const cutoff = new Date(Date.now() - 72 * 60 * 60 * 1000).toISOString();
      const { data: matches, error: fetchErr } = await supabaseAdmin
        .from("matches")
        .select("id, home_team_name, away_team_name, competition_name, status, highlight_video_id")
        .eq("status", "FINISHED")
        .is("highlight_video_id", null)
        .gte("utc_date", cutoff);

      if (fetchErr) {
        console.error(`[WebSub Push] Supabase query error: ${fetchErr.message}`);
        return new Response("DB Error", { status: 500 });
      }

      let matchedMatch = null;
      for (const m of matches || []) {
        const hName = (m.home_team_name || "").toLowerCase().replace(/fc|cf|afc/g, "").trim();
        const aName = (m.away_team_name || "").toLowerCase().replace(/fc|cf|afc/g, "").trim();

        // Check if both teams or one team + highlight keyword appear in title
        const hasHome = lower.includes(hName) && hName.length >= 3;
        const hasAway = lower.includes(aName) && aName.length >= 3;
        const hasHighlightWord = lower.includes("highlight") || lower.includes("resumen") || /\b\d+\s*[-–]\s*\d+\b/.test(lower);

        if (hasHome && hasAway && hasHighlightWord) {
          matchedMatch = m;
          break;
        }
      }

      if (matchedMatch) {
        console.log(`[WebSub Push] MATCH LINKED! Linking ${videoId} to match ${matchedMatch.id}: ${matchedMatch.home_team_name} vs ${matchedMatch.away_team_name}`);
        const { error: updateErr } = await supabaseAdmin
          .from("matches")
          .update({
            highlight_video_id: videoId,
            updated_at: new Date().toISOString(),
          })
          .eq("id", matchedMatch.id);

        if (updateErr) {
          console.error(`[WebSub Push] Update error: ${updateErr.message}`);
        } else {
          console.log(`[WebSub Push] Successfully saved highlight for match ${matchedMatch.id}`);
        }
      } else {
        console.log(`[WebSub Push] No active unlinked match found for video: "${title}"`);
      }

      return new Response("Processed", { status: 200 });
    } catch (e) {
      console.error(`[WebSub Push] Processing error: ${e}`);
      return new Response("Internal Server Error", { status: 500 });
    }
  }

  return new Response("Method Not Allowed", { status: 405 });
});
