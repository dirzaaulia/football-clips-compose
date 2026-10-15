from worker.clients.youtube_rss_client import youtube_rss_client


def test_rss_xml_parsing():
    sample_xml = b"""<?xml version="1.0" encoding="UTF-8"?>
    <feed xmlns:yt="http://www.youtube.com/xml/schemas/2015" xmlns="http://www.w3.org/2005/Atom">
      <entry>
        <id>yt:video:TEST_VID_123</id>
        <yt:videoId>TEST_VID_123</yt:videoId>
        <yt:channelId>UC123456</yt:channelId>
        <title>Highlights | Arsenal 3 - 0 Chelsea | Premier League</title>
        <published>2026-10-15T12:00:00+00:00</published>
        <author><name>Arsenal</name></author>
      </entry>
      <entry>
        <id>yt:video:TEST_VID_456</id>
        <yt:videoId>TEST_VID_456</yt:videoId>
        <yt:channelId>UC123456</yt:channelId>
        <title>Inside Training Ahead of Champions League</title>
        <published>2026-10-14T10:00:00+00:00</published>
        <author><name>Arsenal</name></author>
      </entry>
    </feed>
    """
    items = youtube_rss_client._parse_feed_xml(sample_xml, "UC123456")
    assert len(items) == 2
    assert items[0]["id"]["videoId"] == "TEST_VID_123"
    assert "Arsenal 3 - 0 Chelsea" in items[0]["snippet"]["title"]
    assert items[0]["snippet"]["channelTitle"] == "Arsenal"
    assert items[0]["snippet"]["publishedAt"] == "2026-10-15T12:00:00+00:00"
    assert items[1]["id"]["videoId"] == "TEST_VID_456"
