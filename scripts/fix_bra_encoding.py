import pg8000.native

conn = pg8000.native.Connection('postgres', host='100.82.171.21', port=5432, database='footballclips', password='jarvis_postgres_secure_2026')
conn.run("UPDATE competitions SET name = 'Brasileirao Serie A', short_name = 'Brasileirao' WHERE id = 'BRA'")
conn.run("NOTIFY pgrst, 'reload schema'")
conn.close()
print("Set clean ASCII Brasileirao Serie A")
