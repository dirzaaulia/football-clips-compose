-- ==============================================================================
-- FOOTBALL CLIPS - GLOBAL INTERNATIONAL BREAK & WORLDWIDE RIGHTS MIGRATION
-- Season 2026/2027 & Worldwide Unrestricted Highlight Integration
-- ==============================================================================

-- 1. Extend supported_clubs with team_type column
ALTER TABLE IF EXISTS public.supported_clubs
    ADD COLUMN IF NOT EXISTS team_type VARCHAR(20) DEFAULT 'club'
    CHECK (team_type IN ('club', 'national_team'));

CREATE INDEX IF NOT EXISTS idx_supported_clubs_team_type 
    ON public.supported_clubs (team_type, is_active);

-- 2. Seed 8 Global International Break Competitions
INSERT INTO public.competitions (
    id, name, short_name, country_code, emblem_url, is_active, display_order, type, youtube_handle, youtube_channel_id
) VALUES
    ('UNL', 'UEFA Nations League', 'Nations Lge', 'EU', 'https://a.espncdn.com/i/leaguelogos/soccer/500/2395.png', true, 24, 'international', NULL, NULL),
    ('WCQ_CONMEBOL', 'FIFA World Cup Qualifying - CONMEBOL', 'WCQ CONMEBOL', 'SA', 'https://a.espncdn.com/i/leaguelogos/soccer/500/787.png', true, 25, 'international', '@conmebol', 'UCzU8-lZlRfkV3nj0RzAZdrQ'),
    ('WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'WCQ Asia', 'AS', 'https://a.espncdn.com/i/leaguelogos/soccer/500/789.png', true, 26, 'international', '@TheAFCHub', 'UCJD_ajImG_lpeuK6WTvYI0A'),
    ('WCQ_UEFA', 'FIFA World Cup Qualifying - UEFA', 'WCQ Europe', 'EU', 'https://a.espncdn.com/i/leaguelogos/soccer/500/786.png', true, 27, 'international', '@FIFA', 'UCpcTrCXblq78GZrTUTLWeBw'),
    ('CNL', 'Concacaf Nations League', 'Concacaf NL', 'NA', 'https://a.espncdn.com/i/leaguelogos/soccer/500/19267.png', true, 28, 'international', '@concacaf', 'UCqn7r-so0mBLaJTtTms9dAQ'),
    ('WCQ_CONCACAF', 'FIFA World Cup Qualifying - Concacaf', 'WCQ Concacaf', 'NA', 'https://a.espncdn.com/i/leaguelogos/soccer/500/788.png', true, 29, 'international', '@concacaf', 'UCqn7r-so0mBLaJTtTms9dAQ'),
    ('WCQ_CAF', 'FIFA World Cup Qualifying - CAF', 'WCQ Africa', 'AF', 'https://a.espncdn.com/i/leaguelogos/soccer/500/790.png', true, 30, 'international', '@FIFA', 'UCpcTrCXblq78GZrTUTLWeBw'),
    ('FRI', 'International Friendlies', 'Friendlies', 'INT', 'https://a.espncdn.com/i/leaguelogos/soccer/500/3922.png', true, 31, 'international', '@FIFA', 'UCpcTrCXblq78GZrTUTLWeBw')
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    short_name = EXCLUDED.short_name,
    country_code = EXCLUDED.country_code,
    emblem_url = EXCLUDED.emblem_url,
    is_active = EXCLUDED.is_active,
    display_order = EXCLUDED.display_order,
    type = EXCLUDED.type,
    youtube_handle = EXCLUDED.youtube_handle,
    youtube_channel_id = EXCLUDED.youtube_channel_id,
    updated_at = NOW();

-- 3. Seed Verified National Teams into supported_clubs (Tier 1B - 100% Worldwide Watchability)
INSERT INTO public.supported_clubs (
    id, team_id, name, short_name, competition_code, competition_name, crest_url, youtube_handle, youtube_channel_id, team_type, is_active
) VALUES
    -- UEFA
    ('england_nt', 394, 'England', 'England', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/eng.png', '@england', 'UCNT2e7Og56vm5_V-yJWvglA', 'national_team', true),
    ('france_nt', 602, 'France', 'France', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/fra.png', '@fff', 'UCuxiacueNMDlnjIaqwzRh8A', 'national_team', true),
    ('germany_nt', 601, 'Germany', 'Germany', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/ger.png', '@DFB', 'UCfMo0xj-sbdzHuzxvKdu1hw', 'national_team', true),
    ('spain_nt', 603, 'Spain', 'Spain', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/esp.png', '@RFEF', 'UCmo2IxIA_aPfSJDPrTHyhKw', 'national_team', true),
    ('portugal_nt', 605, 'Portugal', 'Portugal', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/por.png', '@FPF.Oficial', 'UCsIoK3XP-cVcpoCC_SdjZdg', 'national_team', true),
    ('netherlands_nt', 604, 'Netherlands', 'Netherlands', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/ned.png', '@onsoranje', 'UCpnmJcBhJqKIHFkKvgdkdMQ', 'national_team', true),
    ('italy_nt', 606, 'Italy', 'Italy', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/ita.png', '@nazionaledicalcio', 'UCUnVodBnteAdGONNcUpS_kw', 'national_team', true),
    ('belgium_nt', 607, 'Belgium', 'Belgium', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/bel.png', '@royalbelgianfa', 'UCtH7VXC4kowqhH2-RMq0F-Q', 'national_team', true),
    ('croatia_nt', 608, 'Croatia', 'Croatia', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/cro.png', '@HNSfamily', 'UCxoMqo8zzWXuVmnSV3A9h0g', 'national_team', true),
    ('denmark_nt', 609, 'Denmark', 'Denmark', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/den.png', '@DBUTV', 'UCdQhkwKtR4mFnFQILvYz-Rg', 'national_team', true),
    ('austria_nt', 610, 'Austria', 'Austria', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/aut.png', '@oefbtv', 'UChj8TnR6PteEf4IewhD46RQ', 'national_team', true),
    ('sweden_nt', 611, 'Sweden', 'Sweden', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/swe.png', '@svenskfotboll', 'UCaQHxlbPAmh7VWRudyRkwjw', 'national_team', true),
    ('norway_nt', 612, 'Norway', 'Norway', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/nor.png', '@NFFtv', 'UCsPUqY95-2pNQDxsSBMRdhQ', 'national_team', true),
    ('switzerland_nt', 613, 'Switzerland', 'Switzerland', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/sui.png', '@SwissFootball', 'UCOEVXsRKISs84BVdHsKF0RA', 'national_team', true),
    ('poland_nt', 614, 'Poland', 'Poland', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/pol.png', '@LaczyNasPilka', 'UCcz1Tizq8IFdEguy3smvnhA', 'national_team', true),
    ('scotland_nt', 615, 'Scotland', 'Scotland', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/sco.png', '@scotlandnationalteam', 'UChaFW_N-7dndut-HC4Exe-g', 'national_team', true),
    ('wales_nt', 616, 'Wales', 'Wales', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/wal.png', '@fawales', 'UCY5VnOF7zVCP09PKYCqXXuQ', 'national_team', true),
    ('ireland_nt', 617, 'Ireland', 'Ireland', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/irl.png', '@fai', 'UCtcyT50rxyGPXeA5fCporQQ', 'national_team', true),
    ('northern_ireland_nt', 618, 'Northern Ireland', 'N. Ireland', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/nir.png', '@OfficialIrishFA', 'UCw3h5e-kTYzX3WWBYwv37PA', 'national_team', true),
    ('turkey_nt', 619, 'Turkey', 'Turkey', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/tur.png', '@TFF_Org', 'UCc_eoLoYY1UfljdXKQZb5Fg', 'national_team', true),
    ('ukraine_nt', 620, 'Ukraine', 'Ukraine', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/ukr.png', '@uafukraine', 'UCZmbPSRV2FOOPc8PXhoJkPw', 'national_team', true),
    ('czechia_nt', 621, 'Czechia', 'Czechia', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/cze.png', '@ceskarepre', 'UC2PsLB2Uva-vviwxqQtWHRw', 'national_team', true),
    ('slovakia_nt', 622, 'Slovakia', 'Slovakia', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/svk.png', '@sfzofficial', 'UC7ClCWq8jqGOtDUWQyPZidg', 'national_team', true),
    ('hungary_nt', 623, 'Hungary', 'Hungary', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/hun.png', '@MLSZTV', 'UCRprm9irsnl0y-xS96VudQw', 'national_team', true),
    ('romania_nt', 624, 'Romania', 'Romania', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/rou.png', '@FRFTVOfficial', 'UCthNI7cE3k5nTodcga2yBZg', 'national_team', true),
    ('serbia_nt', 625, 'Serbia', 'Serbia', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/srb.png', '@FSSrbije', 'UCZNXXPxDaNbc1mzr7g_nBSg', 'national_team', true),
    ('greece_nt', 626, 'Greece', 'Greece', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/gre.png', '@EthnikiOmada', 'UC1Y2wf1sefOhOqUEGiesujQ', 'national_team', true),
    ('albania_nt', 627, 'Albania', 'Albania', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/alb.png', '@fshf', 'UCexrwl0I-6vT75rPcBJ1J9A', 'national_team', true),
    ('bosnia_nt', 628, 'Bosnia and Herzegovina', 'Bosnia', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/bih.png', '@nfsbih', 'UCPqVGgWxHiYYNduAfsetw-g', 'national_team', true),
    ('north_macedonia_nt', 629, 'North Macedonia', 'N. Macedonia', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/mkd.png', '@FFMOfficial', 'UCMy97yqcofsUqx2iPwRgsmw', 'national_team', true),
    ('kosovo_nt', 630, 'Kosovo', 'Kosovo', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/kos.png', '@FederataeFutbolliteKosoves', 'UCzeT6tNab6BQ38KZvvNYyKA', 'national_team', true),
    ('iceland_nt', 631, 'Iceland', 'Iceland', 'UNL', 'UEFA Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/isl.png', '@footballiceland', 'UC4gvUo8JWCDIUZPL4f7Go9w', 'national_team', true),

    -- CONMEBOL
    ('argentina_nt', 202, 'Argentina', 'Argentina', 'WCQ_CONMEBOL', 'FIFA World Cup Qualifying - CONMEBOL', 'https://a.espncdn.com/i/teamlogos/countries/500/arg.png', '@afaseleccion', 'UCM8nHnLlZ6SNnW3WYsp6WAQ', 'national_team', true),
    ('bolivia_nt', 204, 'Bolivia', 'Bolivia', 'WCQ_CONMEBOL', 'FIFA World Cup Qualifying - CONMEBOL', 'https://a.espncdn.com/i/teamlogos/countries/500/bol.png', NULL, NULL, 'national_team', true),
    ('brazil_nt', 205, 'Brazil', 'Brazil', 'WCQ_CONMEBOL', 'FIFA World Cup Qualifying - CONMEBOL', 'https://a.espncdn.com/i/teamlogos/countries/500/bra.png', '@brasil', 'UCdQuDaRww5NkKpQQ1BJBWww', 'national_team', true),
    ('chile_nt', 207, 'Chile', 'Chile', 'WCQ_CONMEBOL', 'FIFA World Cup Qualifying - CONMEBOL', 'https://a.espncdn.com/i/teamlogos/countries/500/chi.png', '@LaRojaOficial', 'UCILn5WY8gE9zL8QqolYhR8A', 'national_team', true),
    ('colombia_nt', 208, 'Colombia', 'Colombia', 'WCQ_CONMEBOL', 'FIFA World Cup Qualifying - CONMEBOL', 'https://a.espncdn.com/i/teamlogos/countries/500/col.png', '@fcfseleccioncol', 'UCbDc13_3u1bLWDRd1PJGGVA', 'national_team', true),
    ('ecuador_nt', 209, 'Ecuador', 'Ecuador', 'WCQ_CONMEBOL', 'FIFA World Cup Qualifying - CONMEBOL', 'https://a.espncdn.com/i/teamlogos/countries/500/ecu.png', '@FEFecuador', 'UCT2SENSG7HLpR8MWkH-RV7A', 'national_team', true),
    ('paraguay_nt', 210, 'Paraguay', 'Paraguay', 'WCQ_CONMEBOL', 'FIFA World Cup Qualifying - CONMEBOL', 'https://a.espncdn.com/i/teamlogos/countries/500/par.png', '@APFOficial', 'UCGl7qfFQmsXIz0j5O-BEeAg', 'national_team', true),
    ('peru_nt', 211, 'Peru', 'Peru', 'WCQ_CONMEBOL', 'FIFA World Cup Qualifying - CONMEBOL', 'https://a.espncdn.com/i/teamlogos/countries/500/per.png', '@FPFOficial', 'UCqZ2ZN4s66Hma0aIbS-fBSA', 'national_team', true),
    ('uruguay_nt', 212, 'Uruguay', 'Uruguay', 'WCQ_CONMEBOL', 'FIFA World Cup Qualifying - CONMEBOL', 'https://a.espncdn.com/i/teamlogos/countries/500/uru.png', '@AUFoficial', 'UC0jQd1_qQAT4an-dDaG1Sww', 'national_team', true),
    ('venezuela_nt', 213, 'Venezuela', 'Venezuela', 'WCQ_CONMEBOL', 'FIFA World Cup Qualifying - CONMEBOL', 'https://a.espncdn.com/i/teamlogos/countries/500/ven.png', '@LaVinotintoTV', 'UC22-d40NDHfO7oFwvBqivNg', 'national_team', true),

    -- CONCACAF
    ('usa_nt', 648, 'United States', 'USA', 'CNL', 'Concacaf Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/usa.png', '@ussoccer', 'UCk1pcWQ5E19g0Cgp4c1eI1w', 'national_team', true),
    ('mexico_nt', 203, 'Mexico', 'Mexico', 'CNL', 'Concacaf Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/mex.png', '@miseleccionmx', 'UC3D3rXIt1zy-TC_wJ3V8Z_w', 'national_team', true),
    ('canada_nt', 206, 'Canada', 'Canada', 'CNL', 'Concacaf Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/can.png', '@CanadaSoccerTV', 'UCPwOPM0PbkY0S1V_OqA-xjg', 'national_team', true),
    ('costa_rica_nt', 214, 'Costa Rica', 'Costa Rica', 'CNL', 'Concacaf Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/crc.png', '@fcrf', 'UCHIN8JdI7BvFuyjvxS_KdKA', 'national_team', true),
    ('panama_nt', 215, 'Panama', 'Panama', 'CNL', 'Concacaf Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/pan.png', '@FEPAFUT', 'UCocrgqI1pA9-M5b7qXv_QFA', 'national_team', true),
    ('honduras_nt', 216, 'Honduras', 'Honduras', 'CNL', 'Concacaf Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/hon.png', '@fenafuthorg', 'UC2tue1-djqOKs8sPuWW7InQ', 'national_team', true),
    ('guatemala_nt', 217, 'Guatemala', 'Guatemala', 'CNL', 'Concacaf Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/gua.png', '@fedefutguate', 'UCsYMF2Y0e7mPLuFpUD6YJYw', 'national_team', true),
    ('jamaica_nt', 218, 'Jamaica', 'Jamaica', 'CNL', 'Concacaf Nations League', 'https://a.espncdn.com/i/teamlogos/countries/500/jam.png', '@jff_football', 'UCsZIMYuSIiwx8pk62qKmqRQ', 'national_team', true),

    -- AFC
    ('indonesia_nt', 2362, 'Indonesia', 'Indonesia', 'WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'https://a.espncdn.com/i/teamlogos/countries/500/idn.png', '@TimnasIndonesia_Official', 'UCzk65VKtQ4cpNk-K0AnKSyg', 'national_team', true),
    ('japan_nt', 632, 'Japan', 'Japan', 'WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'https://a.espncdn.com/i/teamlogos/countries/500/jpn.png', '@jfa', 'UCgIeUSV91-FfmCayG4lSBcw', 'national_team', true),
    ('south_korea_nt', 633, 'South Korea', 'Korea', 'WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'https://a.espncdn.com/i/teamlogos/countries/500/kor.png', '@thekfa', 'UCqmmurJfP4oRh4d92C6xMfQ', 'national_team', true),
    ('australia_nt', 634, 'Australia', 'Australia', 'WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'https://a.espncdn.com/i/teamlogos/countries/500/aus.png', '@Socceroos', 'UCjHeEbVgbX_EAAhAtkLMTew', 'national_team', true),
    ('saudi_arabia_nt', 635, 'Saudi Arabia', 'Saudi Arabia', 'WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'https://a.espncdn.com/i/teamlogos/countries/500/ksa.png', '@SaudiNT', 'UCwZnmRAjpvRUS0IFTwWwldA', 'national_team', true),
    ('qatar_nt', 636, 'Qatar', 'Qatar', 'WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'https://a.espncdn.com/i/teamlogos/countries/500/qat.png', '@qfa', 'UCur_OGrIuNr1KxMq-6Q96SA', 'national_team', true),
    ('uzbekistan_nt', 637, 'Uzbekistan', 'Uzbekistan', 'WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'https://a.espncdn.com/i/teamlogos/countries/500/uzb.png', '@FAUzbekistan', 'UCqYUp5Rnnp4-0SCMu_ZltXQ', 'national_team', true),
    ('iraq_nt', 638, 'Iraq', 'Iraq', 'WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'https://a.espncdn.com/i/teamlogos/countries/500/irq.png', '@iraqfa', 'UCBnZ16ahKA2DZ_T5W0FPUXg', 'national_team', true),
    ('thailand_nt', 639, 'Thailand', 'Thailand', 'WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'https://a.espncdn.com/i/teamlogos/countries/500/tha.png', '@changsuek', 'UCux9LfrpZSYClHFBbPbnniA', 'national_team', true),
    ('malaysia_nt', 640, 'Malaysia', 'Malaysia', 'WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'https://a.espncdn.com/i/teamlogos/countries/500/mas.png', '@FAMTVMalaysia', 'UC20vnIGa_W4ofoLtKKzhvfA', 'national_team', true),
    ('singapore_nt', 641, 'Singapore', 'Singapore', 'WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'https://a.espncdn.com/i/teamlogos/countries/500/sgp.png', '@FASg', 'UCiAT8XB4TXKE7FnueCu45Kw', 'national_team', true),
    ('philippines_nt', 642, 'Philippines', 'Philippines', 'WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'https://a.espncdn.com/i/teamlogos/countries/500/phi.png', '@ThePFF', 'UCVNZiriffSjWOORykljswNA', 'national_team', true),
    ('india_nt', 643, 'India', 'India', 'WCQ_AFC', 'FIFA World Cup Qualifying - AFC', 'https://a.espncdn.com/i/teamlogos/countries/500/ind.png', '@IndianFootball', 'UCjiPxzIyNtfQ2HZZ1eVjZlg', 'national_team', true),

    -- CAF
    ('morocco_nt', 644, 'Morocco', 'Morocco', 'WCQ_CAF', 'FIFA World Cup Qualifying - CAF', 'https://a.espncdn.com/i/teamlogos/countries/500/mar.png', '@FRMFOfficiel', 'UC_ZMGMdEVWKGWhwMaWKP4Xg', 'national_team', true),
    ('senegal_nt', 645, 'Senegal', 'Senegal', 'WCQ_CAF', 'FIFA World Cup Qualifying - CAF', 'https://a.espncdn.com/i/teamlogos/countries/500/sen.png', '@fsfofficielle', 'UCBnBRS_fdrIQFxW6w2BiG1g', 'national_team', true),
    ('egypt_nt', 646, 'Egypt', 'Egypt', 'WCQ_CAF', 'FIFA World Cup Qualifying - CAF', 'https://a.espncdn.com/i/teamlogos/countries/500/egy.png', '@efa', 'UCSJDypxXUs9AdXZsh2dHPTA', 'national_team', true),
    ('nigeria_nt', 647, 'Nigeria', 'Nigeria', 'WCQ_CAF', 'FIFA World Cup Qualifying - CAF', 'https://a.espncdn.com/i/teamlogos/countries/500/nga.png', '@thenff', 'UCYMCVnzHyETr8Vkuo2l6vfQ', 'national_team', true),
    ('ghana_nt', 649, 'Ghana', 'Ghana', 'WCQ_CAF', 'FIFA World Cup Qualifying - CAF', 'https://a.espncdn.com/i/teamlogos/countries/500/gha.png', '@ghanafa', 'UClUS-CwPe5SCR9xvbPd_uLw', 'national_team', true),
    ('ivory_coast_nt', 650, 'Ivory Coast', 'Ivory Coast', 'WCQ_CAF', 'FIFA World Cup Qualifying - CAF', 'https://a.espncdn.com/i/teamlogos/countries/500/civ.png', '@fifci', 'UCQ2cjbZOB-An8k6w0yYNDNg', 'national_team', true),
    ('cameroon_nt', 651, 'Cameroon', 'Cameroon', 'WCQ_CAF', 'FIFA World Cup Qualifying - CAF', 'https://a.espncdn.com/i/teamlogos/countries/500/cmr.png', '@FecafootOfficiel', 'UCskTN9JqyrOR0dUym4402JQ', 'national_team', true),
    ('algeria_nt', 652, 'Algeria', 'Algeria', 'WCQ_CAF', 'FIFA World Cup Qualifying - CAF', 'https://a.espncdn.com/i/teamlogos/countries/500/alg.png', '@faftv', 'UCPqJruL_ZAz0lkudQCD2OEQ', 'national_team', true),
    ('south_africa_nt', 653, 'South Africa', 'South Africa', 'WCQ_CAF', 'FIFA World Cup Qualifying - CAF', 'https://a.espncdn.com/i/teamlogos/countries/500/rsa.png', '@safatv', 'UC49nSrMOkPsGVR9GPTVBVRA', 'national_team', true),

    -- OFC
    ('new_zealand_nt', 654, 'New Zealand', 'New Zealand', 'FRI', 'International Friendlies', 'https://a.espncdn.com/i/teamlogos/countries/500/nzl.png', '@NZFootball', 'UCYd2DFvQ3Ov2qvFNQu9WLYw', 'national_team', true)
ON CONFLICT (id) DO UPDATE SET
    team_id = EXCLUDED.team_id,
    name = EXCLUDED.name,
    short_name = EXCLUDED.short_name,
    competition_code = EXCLUDED.competition_code,
    competition_name = EXCLUDED.competition_name,
    crest_url = EXCLUDED.crest_url,
    youtube_handle = EXCLUDED.youtube_handle,
    youtube_channel_id = EXCLUDED.youtube_channel_id,
    team_type = EXCLUDED.team_type,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- 4. Seed Regional Broadcasters into broadcasters table (Tier 2 Failover)
INSERT INTO public.broadcasters (
    broadcaster_id, name, channel_handle, channel_id, allowed_countries, competitions, status
) VALUES
    -- Indonesia
    ('rcti_id', 'RCTI', '@OfficialRCTI', 'UCzTsWuCdVP_vehWyGwPcS3Q', '["ID"]'::jsonb, '["UNL", "WCQ_UEFA", "WCQ_AFC", "FRI"]'::jsonb, 'active'),
    ('inews_id', 'iNews', '@OfficialiNews', 'UC_7n5QZdUIpPc0-C_s9hshg', '["ID"]'::jsonb, '["UNL", "WCQ_UEFA", "WCQ_AFC", "FRI"]'::jsonb, 'active'),
    -- Malaysia & Brunei
    ('astro_arena', 'Astro Arena', '@AstroArena801', 'UCU9GQSAhb4JxdSZKNJ_Ib6w', '["MY", "BN"]'::jsonb, '["UNL", "WCQ_AFC", "FRI"]'::jsonb, 'active'),
    -- Thailand
    ('thairath_th', 'Thairath Sport', '@ThairathSport', 'UCrFDdD-EE05N7gjwZho2wqw', '["TH"]'::jsonb, '["WCQ_AFC", "FRI"]'::jsonb, 'active'),
    -- UK & Ireland
    ('itv_sport_gb', 'ITV Sport', '@itvsport', 'UCfYxZOdUz-zwmsjVKplsKxQ', '["GB", "IE"]'::jsonb, '["UNL", "WCQ_UEFA", "FRI"]'::jsonb, 'active'),
    ('premier_sports_gb', 'Premier Sports UK', '@PremierSportsTV', 'UC5KgQoGxsCJ6zVwetWvqEEQ', '["GB", "IE"]'::jsonb, '["UNL", "WCQ_UEFA"]'::jsonb, 'active'),
    -- Spain
    ('movistar_es', 'Movistar Plus+', '@MovistarPlus', 'UCPgvCUSmHWm2177LkwLtZQw', '["ES"]'::jsonb, '["UNL", "WCQ_UEFA", "FRI"]'::jsonb, 'active'),
    -- Germany / DACH
    ('dazn_de', 'DAZN Football', '@DAZNFussball', 'UCB-GdMjyokO9lZkKU_oIK6g', '["DE", "AT", "CH"]'::jsonb, '["UNL", "WCQ_UEFA", "WCQ_CONMEBOL", "FRI"]'::jsonb, 'active'),
    ('sportschau_de', 'ARD Sportschau', '@sportschau', 'UCRFsyeKu07-LnHDG44O6uCA', '["DE"]'::jsonb, '["UNL", "WCQ_UEFA", "FRI"]'::jsonb, 'active'),
    -- Italy
    ('skysport_it', 'Sky Sport Italia', '@SkySport', 'UCAvNr5DMVncoJnJAotXOjOQ', '["IT"]'::jsonb, '["UNL", "WCQ_UEFA", "FRI"]'::jsonb, 'active'),
    -- France
    ('lequipe_fr', 'L Equipe', '@lequipe', 'UCCio4yaFxDNGMHu9CE0PYJg', '["FR"]'::jsonb, '["UNL", "WCQ_UEFA"]'::jsonb, 'active'),
    -- Portugal
    ('sporttv_pt', 'Sport TV Portugal', '@SportTVPortugal', 'UCINrlkmrXi4a-kOl6unb51A', '["PT"]'::jsonb, '["UNL", "WCQ_UEFA", "WCQ_CONMEBOL", "FRI"]'::jsonb, 'active'),
    -- Netherlands
    ('ziggo_nl', 'Ziggo Sport', '@ZiggoSport', 'UCv4re-peeCgLz3m1OJcZirA', '["NL"]'::jsonb, '["UNL", "WCQ_UEFA", "FRI"]'::jsonb, 'active'),
    -- Turkey
    ('trt_spor_tr', 'TRT Spor', '@trtspor', 'UCebdo7-2NdjcktKzco64iNw', '["TR"]'::jsonb, '["UNL", "WCQ_UEFA", "FRI"]'::jsonb, 'active'),
    -- USA & North America
    ('fox_sports_us', 'FOX Sports', '@foxsports', 'UCvQrivswRDGK0lZ_AcUHp8g', '["US"]'::jsonb, '["UNL", "WCQ_UEFA", "WCQ_CONMEBOL", "FRI"]'::jsonb, 'active'),
    ('cbs_golazo_us', 'CBS Sports Golazo', '@CBSSportsGolazo', 'UCX_tjI6Q_4JD1E3234CwemA', '["US"]'::jsonb, '["CNL", "WCQ_CONCACAF", "UNL"]'::jsonb, 'active'),
    ('tudn_us_mx', 'TUDN', '@TUDN', 'UCy5wECS1fIpQbwKawR3908A', '["US", "MX"]'::jsonb, '["CNL", "WCQ_CONCACAF", "UNL", "FRI"]'::jsonb, 'active'),
    ('telemundo_us', 'Telemundo Deportes', '@deportes', 'UCjZ7QPKb89R-4SxzBoceyOg', '["US"]'::jsonb, '["WCQ_CONMEBOL", "CNL", "FRI"]'::jsonb, 'active'),
    -- South America
    ('cazetv_br', 'CazeTV', '@CazeTV', 'UCZiYbVptd3PVPf4f6eR6UaQ', '["BR"]'::jsonb, '["UNL", "WCQ_UEFA", "WCQ_CONMEBOL", "FRI"]'::jsonb, 'active'),
    ('espn_brasil', 'ESPN Brasil', '@espnbrasil', 'UCw5-xj3AKqEizC7MvHaIPqA', '["BR"]'::jsonb, '["UNL", "WCQ_UEFA", "FRI"]'::jsonb, 'active'),
    ('tyc_sports_ar', 'TyC Sports', '@TyCSports', 'UC72ZaBKI-Bo5fjmWEYonhJw', '["AR"]'::jsonb, '["WCQ_CONMEBOL", "FRI"]'::jsonb, 'active'),
    ('gol_caracol_co', 'Gol Caracol', '@GolCaracol', 'UCXXOzmyYvDtaijm5i303Tng', '["CO"]'::jsonb, '["WCQ_CONMEBOL", "FRI"]'::jsonb, 'active'),
    ('chilevision_cl', 'Chilevision', '@chilevision', 'UC8EdTmyUaFIfZvVttJ9lgIA', '["CL"]'::jsonb, '["WCQ_CONMEBOL", "FRI"]'::jsonb, 'active'),
    -- MENA
    ('bein_sports_mena', 'beIN SPORTS MENA', '@beINSPORTS', 'UCJUCcJUeh0Cz2xyKwkw5Q1w', '["SA", "AE", "EG", "MA", "QA", "KW", "OM", "BH", "JO", "LB", "IQ", "DZ", "TN"]'::jsonb, '["UNL", "WCQ_UEFA", "WCQ_CAF", "WCQ_AFC", "FRI"]'::jsonb, 'active'),
    ('ssc_sports_sa', 'SSC Sports', '@ssc_sports', 'UCXichv4XKps2gKNGh7yVPIw', '["SA"]'::jsonb, '["WCQ_AFC", "FRI"]'::jsonb, 'active'),
    -- Pan-Africa
    ('supersport_africa', 'SuperSport Football', '@SuperSportFootball', 'UCGx26yfN8hc1n_wPZA8Seqw', '["ZA", "NG", "GH", "KE", "UG", "TZ", "ZM", "ZW", "NA", "BW"]'::jsonb, '["UNL", "WCQ_UEFA", "WCQ_CAF", "FRI"]'::jsonb, 'active'),
    ('sportytv_africa', 'SportyTV', '@SportyTV', 'UCfddQxw3tS6oQWdgrExTrnw', '["NG", "GH", "KE"]'::jsonb, '["WCQ_CAF", "FRI"]'::jsonb, 'active'),
    -- Asia-Pacific & Subcontinent
    ('dazn_japan', 'DAZN Japan', '@DAZNJapan', 'UCyeDNNizMGbVsn_8Ttc3FIw', '["JP"]'::jsonb, '["WCQ_AFC", "UNL", "FRI"]'::jsonb, 'active'),
    ('tvn_sports_kr', 'tvN Sports', '@tvNSports', 'UCtybqqaTj6Nx74Azdz1KrsA', '["KR"]'::jsonb, '["WCQ_AFC", "UNL", "FRI"]'::jsonb, 'active'),
    ('sony_sports_in', 'Sony Sports Network', '@SonySportsNetwork', 'UC_WKb6N9iTGc77hxwXLDrbA', '["IN", "PK", "BD", "LK", "NP"]'::jsonb, '["UNL", "WCQ_UEFA", "FRI"]'::jsonb, 'active'),
    ('sports18_in', 'Sports18', '@Sports18', 'UCC9g0QnifCL-ptPE1YsAKNQ', '["IN"]'::jsonb, '["WCQ_AFC", "FRI"]'::jsonb, 'active')
ON CONFLICT (broadcaster_id) DO UPDATE SET
    name = EXCLUDED.name,
    channel_handle = EXCLUDED.channel_handle,
    channel_id = EXCLUDED.channel_id,
    allowed_countries = EXCLUDED.allowed_countries,
    competitions = EXCLUDED.competitions,
    status = EXCLUDED.status;

NOTIFY pgrst, 'reload schema';
