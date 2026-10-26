import pg8000.native

def main():
    conn = pg8000.native.Connection(
        'postgres',
        host='100.82.171.21',
        port=5432,
        database='footballclips',
        password='jarvis_postgres_secure_2026'
    )
    print("Connected to PostgreSQL successfully.")

    # 1. Update broken cup / European logos with verified ESPN CDN URLs
    updated_logos = {
        'ECL': 'https://a.espncdn.com/i/leaguelogos/soccer/500/20296.png',
        'FAC': 'https://a.espncdn.com/i/leaguelogos/soccer/500/40.png',
        'EFL': 'https://a.espncdn.com/i/leaguelogos/soccer/500/41.png',
        'CDR': 'https://a.espncdn.com/i/leaguelogos/soccer/500/80.png',
        'DFB': 'https://a.espncdn.com/i/leaguelogos/soccer/500/2061.png',
    }

    print("Updating broken logos with verified ESPN CDN...")
    for comp_id, logo_url in updated_logos.items():
        conn.run(
            "UPDATE competitions SET emblem_url = :url, updated_at = NOW() WHERE id = :id",
            url=logo_url,
            id=comp_id
        )

    # 2. Insert or update South American competitions
    # LIB: CONMEBOL Libertadores
    # SUD: CONMEBOL Sudamericana
    # REC: CONMEBOL Recopa
    # BRA: Brasileirão Série A
    # ARG: Argentine Liga Profesional
    new_competitions = [
        ('LIB', 'CONMEBOL Libertadores', 'Libertadores', 'hybrid', 'SA', 'https://a.espncdn.com/i/leaguelogos/soccer/500/58.png', 19),
        ('SUD', 'CONMEBOL Sudamericana', 'Sudamericana', 'cup', 'SA', 'https://a.espncdn.com/i/leaguelogos/soccer/500/1208.png', 20),
        ('REC', 'CONMEBOL Recopa', 'Recopa', 'cup', 'SA', 'https://a.espncdn.com/i/leaguelogos/soccer/500/2335.png', 21),
        ('BRA', 'Brasileir\u00e3o S\u00e9rie A', 'Brasileir\u00e3o', 'league', 'BR', 'https://a.espncdn.com/i/leaguelogos/soccer/500/85.png', 22),
        ('ARG', 'Argentine Liga Profesional', 'Liga Argentina', 'league', 'AR', 'https://a.espncdn.com/i/leaguelogos/soccer/500/1.png', 23),
    ]

    upsert_sql = """
    INSERT INTO competitions (id, name, short_name, type, country_code, emblem_url, is_active, display_order)
    VALUES (:id, :name, :short_name, :type, :country_code, :emblem_url, true, :display_order)
    ON CONFLICT (id) DO UPDATE SET
        name = EXCLUDED.name,
        short_name = EXCLUDED.short_name,
        type = EXCLUDED.type,
        country_code = EXCLUDED.country_code,
        emblem_url = EXCLUDED.emblem_url,
        is_active = EXCLUDED.is_active,
        display_order = EXCLUDED.display_order,
        updated_at = NOW();
    """

    print("Upserting South American competitions...")
    for code, name, short_name, comp_type, country, emblem, display_order in new_competitions:
        conn.run(
            upsert_sql,
            id=code,
            name=name,
            short_name=short_name,
            type=comp_type,
            country_code=country,
            emblem_url=emblem,
            display_order=display_order
        )

    # 3. Reload PostgREST schema cache
    print("Reloading PostgREST schema cache...")
    conn.run("NOTIFY pgrst, 'reload schema';")

    # 4. Verify all competitions
    print("\n--- All Active Competitions in Datastore ---")
    rows = conn.run("SELECT id, name, short_name, type, emblem_url, display_order FROM competitions ORDER BY display_order ASC")
    for r in rows:
        print(f"{r[0]:5} | {r[1]:30} | {r[2]:15} | {r[3]:8} | {r[5]:2} | {r[4]}")

    conn.close()
    print("\nMigration completed successfully!")

if __name__ == "__main__":
    main()
