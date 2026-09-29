package com.example.data

data class PythonSnippet(
    val title: String,
    val category: String,
    val description: String,
    val code: String
)

object SnippetLibrary {
    val snippets = listOf(
        PythonSnippet(
            title = "Fibonacci Generator",
            category = "Algorithms",
            description = "Calculates n terms of Fibonacci sequence using list comprehension & loop",
            code = """# Fibonacci Generator
def get_fibonacci(n):
    sequence = [0, 1]
    while len(sequence) < n:
        sequence.append(sequence[-1] + sequence[-2])
    return sequence[:n]

count = int(input("How many numbers? ") or "10")
print(f"Fibonacci({count}):", get_fibonacci(count))
"""
        ),
        PythonSnippet(
            title = "Prime Number Checker",
            category = "Math",
            description = "Checks whether a number is prime and finds all primes up to N",
            code = """# Prime Numbers Finder
def is_prime(num):
    if num < 2:
        return False
    for i in range(2, int(num ** 0.5) + 1):
        if num % i == 0:
            return False
    return True

limit = int(input("Find primes up to: ") or "50")
primes = [x for x in range(2, limit + 1) if is_prime(x)]
print(f"Primes up to {limit}:", primes)
print(f"Total primes found: {len(primes)}")
"""
        ),
        PythonSnippet(
            title = "Number Guessing Game",
            category = "Games",
            description = "Interactive CLI game with random numbers and attempts counter",
            code = """# Guessing Game
import random

target = random.randint(1, 100)
attempts = 0
max_attempts = 7

print("🎯 Guess a number between 1 and 100!")
while attempts < max_attempts:
    attempts += 1
    val = input(f"[{attempts}/{max_attempts}] Your guess: ")
    try:
        guess = int(val)
    except ValueError:
        print("Please enter a valid number!")
        continue

    if guess == target:
        print(f"🎉 Correct! You won in {attempts} attempt(s)!")
        break
    elif guess < target:
        print("Too low! ⬆️")
    else:
        print("Too high! ⬇️")
else:
    print(f"😢 Game over! The secret number was {target}.")
"""
        ),
        PythonSnippet(
            title = "Rock, Paper, Scissors",
            category = "Games",
            description = "Classic game against computer with random choice",
            code = """# Rock, Paper, Scissors
import random

choices = ["rock", "paper", "scissors"]
user = input("Choose (rock / paper / scissors): ").lower().strip()
bot = random.choice(choices)

print(f"You: {user} | Bot: {bot}")

if user == bot:
    print("🤝 It's a Tie!")
elif (user == "rock" and bot == "scissors") or \
     (user == "paper" and bot == "rock") or \
     (user == "scissors" and bot == "paper"):
    print("🏆 You Win!")
else:
    print("🤖 Bot Wins!")
"""
        ),
        PythonSnippet(
            title = "String & Word Counter",
            category = "Utilities",
            description = "Counts words, letters, vowels and reverses strings",
            code = """# Text Analysis Tool
text = input("Enter text to analyze: ") or "Python is awesome and versatile!"

words = text.split()
char_count = len(text)
vowels = sum(1 for c in text.lower() if c in "aeiou")

print("--- Analysis Report ---")
print(f"Original Text: {text}")
print(f"Word Count:    {len(words)}")
print(f"Characters:    {char_count}")
print(f"Vowels:        {vowels}")
print(f"Reversed:      {text[::-1]}")
print(f"Uppercase:     {text.upper()}")
"""
        ),
        PythonSnippet(
            title = "Dictionary & JSON Formatter",
            category = "Data",
            description = "Dictionary operations and pretty-printed JSON data",
            code = """# Dictionary & JSON
import json

student = {
    "name": "Alex Mercer",
    "skills": ["Python", "FastAPI", "Machine Learning"],
    "active": True,
    "scores": {"quiz1": 95, "quiz2": 88, "final": 94}
}

# Pretty print JSON format
formatted_json = json.dumps(student, indent=4)
print("Student Profile (JSON):")
print(formatted_json)

average_score = sum(student["scores"].values()) / len(student["scores"])
print(f"\nAverage Score: {average_score:.2f}")
"""
        ),
        PythonSnippet(
            title = "Bubble Sort Visualizer",
            category = "Algorithms",
            description = "Step-by-step sorting algorithm with pass logs",
            code = """# Bubble Sort Algorithm
def bubble_sort(arr):
    n = len(arr)
    print(f"Initial: {arr}")
    for i in range(n):
        swapped = False
        for j in range(0, n - i - 1):
            if arr[j] > arr[j + 1]:
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
                swapped = True
        print(f"Pass {i + 1}: {arr}")
        if not swapped:
            break
    return arr

sample_list = [64, 34, 25, 12, 22, 11, 90]
sorted_list = bubble_sort(sample_list)
print(f"Sorted result: {sorted_list}")
"""
        )
    )
}
