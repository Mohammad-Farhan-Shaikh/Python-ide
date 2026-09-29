package com.example.data

import kotlinx.coroutines.flow.Flow

class FileRepository(private val dao: PythonFileDao) {

    val allFiles: Flow<List<PythonFile>> = dao.getAllFiles()
    val activeFileFlow: Flow<PythonFile?> = dao.getActiveFileFlow()

    suspend fun getActiveFileOnce(): PythonFile? = dao.getActiveFileOnce()

    suspend fun initializeDefaultsIfNeeded() {
        if (dao.getFileCount() == 0) {
            val defaultMain = PythonFile(
                name = "main.py",
                content = """# Welcome to PyStudio Mobile IDE!
# Run, edit, and experiment with Python code.

print("🐍 Hello, Python developer!")

# Interactive input example
name = input("Enter your name: ")
print(f"Awesome to meet you, {name}!")

age_str = input("What is your age? ")
try:
    age = int(age_str)
    years_to_100 = 100 - age
    print(f"In {years_to_100} years, you will be 100 years old! 🚀")
except ValueError:
    print(f"'{age_str}' is not a valid number, but that's okay!")

print("\nCounting powers of 2:")
for i in range(1, 6):
    print(f"  2^{i} = {2 ** i}")

print("\nDone! Feel free to edit this code and press RUN.")
""",
                isActive = true
            )
            val mainId = dao.insertFile(defaultMain)

            val fibFile = PythonFile(
                name = "fibonacci.py",
                content = """# Fibonacci sequence generator

def fibonacci(n):
    sequence = [0, 1]
    while len(sequence) < n:
        sequence.append(sequence[-1] + sequence[-2])
    return sequence[:n]

limit = int(input("How many Fibonacci numbers to generate? ") or "10")
result = fibonacci(limit)
print(f"First {limit} Fibonacci numbers:")
print(result)
""",
                isActive = false
            )
            dao.insertFile(fibFile)

            val gameFile = PythonFile(
                name = "guessing_game.py",
                content = """# Number Guessing Game
import random

target = random.randint(1, 20)
print("🎯 Guess a number between 1 and 20!")

attempts = 0
max_attempts = 5

while attempts < max_attempts:
    attempts += 1
    guess_str = input(f"Attempt {attempts}/{max_attempts} - Your guess: ")
    try:
        guess = int(guess_str)
    except ValueError:
        print("Please enter a valid integer!")
        continue

    if guess == target:
        print(f"🎉 Correct! You found the number in {attempts} attempt(s)!")
        break
    elif guess < target:
        print("Too low! ⬆️")
    else:
        print("Too high! ⬇️")
else:
    print(f"Game over! The target number was {target}.")
""",
                isActive = false
            )
            dao.insertFile(gameFile)

            dao.setActiveFile(mainId)
        }
    }

    suspend fun createNewFile(name: String, content: String = "# New Python Script\n\nprint(\"Hello world!\")\n"): Long {
        val sanitizedName = if (name.endsWith(".py")) name else "$name.py"
        val newFile = PythonFile(
            name = sanitizedName,
            content = content,
            isActive = true
        )
        val id = dao.insertFile(newFile)
        dao.setActiveFile(id)
        return id
    }

    suspend fun switchActiveFile(fileId: Long) {
        dao.setActiveFile(fileId)
    }

    suspend fun autoSaveContent(fileId: Long, content: String) {
        dao.updateContent(fileId, content)
    }

    suspend fun renameFile(fileId: Long, newName: String) {
        val sanitizedName = if (newName.endsWith(".py")) newName else "$newName.py"
        dao.renameFile(fileId, sanitizedName)
    }

    suspend fun deleteFile(file: PythonFile) {
        dao.deleteFile(file)
        if (file.isActive) {
            val remaining = dao.getActiveFileOnce()
            if (remaining == null) {
                // Pick another file or create a fresh main.py
                val all = dao.getAllFiles()
                // If there's another file, activate it
            }
        }
    }
}
