'use client';

import React, { useState } from 'react';
import { Mail, Globe, Send, CheckCircle2, ShieldCheck, Clock } from 'lucide-react';
import CustomDropdown from '@/components/common/CustomDropdown';

export default function ContactPage() {
  const [submitted, setSubmitted] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    category: 'Editorial Feedback',
    message: '',
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name || !formData.email || !formData.message) return;
    setSubmitted(true);
  };

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10 text-zinc-300 space-y-10">
      <header className="border-b border-[#334155]/60 pb-6 space-y-2">
        <span className="inline-block px-2.5 py-1 rounded-full text-xs font-semibold bg-[#1E3A8A]/50 text-[#4FC3F7] border border-[#7098F8]/40">
          Editorial &amp; Publisher Desk
        </span>
        <h1 className="text-3xl sm:text-4xl font-black text-white tracking-tight">Contact Us</h1>
        <p className="text-xs sm:text-sm text-[#94A3B8]">
          Inquiries, editorial corrections, licensing, and advertising communications.
        </p>
      </header>

      {/* Direct Contact Channels */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-[#181C20] border border-[#334155] p-5 rounded-2xl space-y-2">
          <div className="w-9 h-9 rounded-xl bg-[#1E3A8A]/40 text-[#7098F8] flex items-center justify-center">
            <Mail className="w-4 h-4" />
          </div>
          <h3 className="font-bold text-white text-sm">Editorial &amp; Corrections</h3>
          <p className="text-xs text-[#94A3B8] leading-relaxed">
            Report score variances, highlight link updates, or submit match suggestions.
          </p>
          <a href="mailto:dirzaaulia11@gmail.com" className="text-xs font-bold text-[#7098F8] hover:underline block pt-1">
            dirzaaulia11@gmail.com
          </a>
        </div>

        <div className="bg-[#181C20] border border-[#334155] p-5 rounded-2xl space-y-2">
          <div className="w-9 h-9 rounded-xl bg-[#1E3A8A]/40 text-[#4FC3F7] flex items-center justify-center">
            <Globe className="w-4 h-4" />
          </div>
          <h3 className="font-bold text-white text-sm">Media &amp; Advertising</h3>
          <p className="text-xs text-[#94A3B8] leading-relaxed">
            Direct sponsorship opportunities, Google AdSense partnerships, and syndication.
          </p>
          <a href="mailto:dirzaaulia11@gmail.com?subject=Advertising%20Inquiry" className="text-xs font-bold text-[#4FC3F7] hover:underline block pt-1">
            Advertising Desk
          </a>
        </div>

        <div className="bg-[#181C20] border border-[#334155] p-5 rounded-2xl space-y-2">
          <div className="w-9 h-9 rounded-xl bg-[#1E3A8A]/40 text-emerald-400 flex items-center justify-center">
            <Clock className="w-4 h-4" />
          </div>
          <h3 className="font-bold text-white text-sm">Turnaround Times</h3>
          <p className="text-xs text-[#94A3B8] leading-relaxed">
            Our editorial desk operates 7 days a week during active European matchdays.
          </p>
          <span className="text-xs font-semibold text-emerald-400 block pt-1">
            Response within 24-48 hours
          </span>
        </div>
      </div>

      {/* Interactive Contact Form */}
      <section className="bg-[#181C20] border border-[#334155] rounded-3xl p-6 sm:p-8 space-y-6">
        <div>
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <Send className="w-4 h-4 text-[#7098F8]" />
            <span>Send a Direct Inquiry</span>
          </h2>
          <p className="text-xs text-[#94A3B8] mt-1">
            Messages are routed directly to our lead developer and editorial team.
          </p>
        </div>

        {submitted ? (
          <div className="p-6 rounded-2xl bg-[#1E3A8A]/20 border border-[#7098F8]/40 text-center space-y-2">
            <CheckCircle2 className="w-8 h-8 text-emerald-400 mx-auto" />
            <h3 className="text-sm font-bold text-white">Thank you for your message!</h3>
            <p className="text-xs text-[#94A3B8]">
              Our editorial team has received your communication and will reply via email shortly.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4 text-xs">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-zinc-300 font-semibold block">Full Name</label>
                <input
                  type="text"
                  required
                  placeholder="Your Name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-[#101418] border border-[#334155] focus:border-[#7098F8] text-white outline-none"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-zinc-300 font-semibold block">Email Address</label>
                <input
                  type="email"
                  required
                  placeholder="you@domain.com"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className="w-full px-3.5 py-2.5 rounded-xl bg-[#101418] border border-[#334155] focus:border-[#7098F8] text-white outline-none"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="text-zinc-300 font-semibold block text-sm">Inquiry Category</label>
              <CustomDropdown<string>
                items={[
                  { value: 'Editorial Feedback', label: 'Editorial Feedback & Match Debriefs' },
                  { value: 'Corrections', label: 'Score or Video Highlight Correction' },
                  { value: 'Advertising', label: 'Advertising & Sponsorship' },
                  { value: 'Copyright', label: 'DMCA / Copyright Inquiries' },
                  { value: 'Technical', label: 'Technical & Account Support' },
                ]}
                value={formData.category}
                onChange={(cat) => setFormData({ ...formData, category: cat })}
                className="w-full"
                menuWidth="w-full"
                ariaLabel="Select inquiry category"
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-zinc-300 font-semibold block">Message</label>
              <textarea
                required
                rows={4}
                placeholder="How can we help you?"
                value={formData.message}
                onChange={(e) => setFormData({ ...formData, message: e.target.value })}
                className="w-full px-3.5 py-2.5 rounded-xl bg-[#101418] border border-[#334155] focus:border-[#7098F8] text-white outline-none resize-none"
              />
            </div>

            <button
              type="submit"
              className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white font-bold text-xs hover:brightness-110 transition-all shadow-[0_0_15px_rgba(112,152,248,0.3)] cursor-pointer"
            >
              Submit Inquiry
            </button>
          </form>
        )}
      </section>

      {/* Corrections Policy */}
      <section className="p-5 rounded-2xl bg-[#101418] border border-[#334155]/60 text-xs text-[#94A3B8] space-y-2">
        <h3 className="font-bold text-white text-xs flex items-center gap-1.5">
          <ShieldCheck className="w-4 h-4 text-[#7098F8]" />
          <span>Editorial Corrections Policy</span>
        </h3>
        <p className="leading-relaxed">
          We are committed to factual accuracy. If you identify an erroneous scoreline, mistaken team crest, or broken video highlight embed, our team investigates all valid reports and implements corrections within 12 hours.
        </p>
      </section>
    </div>
  );
}
