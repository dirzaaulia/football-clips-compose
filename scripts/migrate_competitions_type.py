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

    # 1. Add type column if not exists
    print("Ensuring column 'type' exists on 'competitions' table...")
    conn.run("ALTER TABLE competitions ADD COLUMN IF NOT EXISTS type TEXT NOT NULL DEFAULT 'league';")

    # 2. Update hybrid competitions
    print("Updating hybrid competitions (CL, EL, ECL)...")
    conn.run("UPDATE competitions SET type = 'hybrid' WHERE id IN ('CL', 'EL', 'ECL');")

    # 3. Update domestic league competitions
    print("Updating league competitions...")
    conn.run("UPDATE competitions SET type = 'league' WHERE id IN ('PL', 'PD', 'SA', 'BL1', 'FL1', 'SPL', 'MLS', 'DED', 'PPL');")
    conn.run("UPDATE competitions SET type = 'league', display_order = 13 WHERE id = 'IDN';")

    # 4. Insert or update knockout cups
    cups = [
        ('FAC', 'FA Cup', 'FA Cup', 'cup', 'GB-ENG', 'https://crests.football-data.org/FAC.png', 14),
        ('EFL', 'Carabao Cup', 'EFL Cup', 'cup', 'GB-ENG', 'https://crests.football-data.org/EFL.png', 15),
        ('CDR', 'Copa del Rey', 'Copa del Rey', 'cup', 'ES', 'https://crests.football-data.org/CDR.png', 16),
        ('DFB', 'DFB-Pokal', 'DFB-Pokal', 'cup', 'DE', 'https://crests.football-data.org/DFB.png', 17),
        ('CIT', 'Coppa Italia', 'Coppa Italia', 'cup', 'IT', 'https://crests.football-data.org/CIT.png', 18),
    ]

    print("Upserting cup competitions...")
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

    for code, name, short_name, comp_type, country, emblem, display_order in cups:
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

    # 5. Reload PostgREST schema cache
    print("Reloading PostgREST schema cache...")
    conn.run("NOTIFY pgrst, 'reload schema';")

    # 6. Verify rows
    print("\n--- Current Competitions in Datastore ---")
    rows = conn.run("SELECT id, name, type, country_code, display_order FROM competitions ORDER BY display_order ASC")
    for r in rows:
        print(f"{r[0]:5} | {r[1]:28} | {r[2]:8} | {r[3]:6} | order: {r[4]}")

    conn.close()
    print("\nMigration completed successfully!")

if __name__ == "__main__":
    main()
