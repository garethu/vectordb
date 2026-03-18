import csv
import psycopg2

# Database connection details
DB_NAME = 'clothing_store'
DB_USER = 'admin'
DB_PASSWORD = 'admin'
DB_HOST = 'localhost'
DB_PORT = '5432'

# File path to the CSV file generated earlier
file_path = 'c:/projects/yootiful/yootiful-vectordbs/data/clothing_combinations.csv'

# Initialize the connection variable
connection = None

try:
    # Establish connection to PostgreSQL database
    connection = psycopg2.connect(
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD,
        host=DB_HOST,
        port=DB_PORT
    )
    cursor = connection.cursor()

    # Open the CSV file and read it
    with open(file_path, 'r') as file:
        reader = csv.reader(file)
        next(reader)  # Skip the header

        # Insert each row into the clothes table
        for row in reader:
            cursor.execute(
                """
                INSERT INTO product (id, uuid, description)
                VALUES (%s, %s, %s)
                """,
                (row[0], row[1], row[2])
            )

    # Commit the transaction
    connection.commit()

    print("Data inserted successfully.")

except Exception as error:
    print(f"Error: {error}")

finally:
    # Ensure the cursor and connection are closed properly
    if connection is not None:
        cursor.close()
        connection.close()
