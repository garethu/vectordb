import csv
import uuid
import random

# File path to save the CSV file
file_path = 'c:/projects/yootiful/yootiful-vectordbs/data/clothing_items.csv'

# Generate 1000 rows with a numerical id, uuid, and description
items = [
    "Trousers", "Jacket", "Skirt", "Blouse"
]
colors = [
    "Black", "Navy Blue", "Red", "White", "Gray", "Green", "Blue", "Pink", "Beige", "Black"
]
materials = [
    "Cotton", "Wool", "Silk", "Linen", "Polyester", "Leather", "Denim", "Nylon", "Satin"
]
seasons = [
    "Summer", "Winter", "Spring", "Fall"
]

# Prepare 1000 rows
rows = []
for i in range(1, 1001):
    item = random.choice(items)  # Randomly pick an item
    color = random.choice(colors)  # Randomly pick a color
    material = random.choice(materials)  # Randomly pick a material
    season = random.choice(seasons)  # Randomly pick a season
    unique_uuid = str(uuid.uuid4())  # Generate a unique UUID
    description = f"{item} with a {color.lower()} color, made from {material.lower()}, and suitable for {season.lower()}."
    rows.append([i, unique_uuid, description])

# Write the rows to a CSV file
with open(file_path, mode='w', newline='') as file:
    writer = csv.writer(file)
    # Write the header
    writer.writerow(["id", "uuid", "description"])
    # Write the data rows
    writer.writerows(rows)

file_path
