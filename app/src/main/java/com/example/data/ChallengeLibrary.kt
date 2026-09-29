package com.example.data

data class PythonChallenge(
    val id: String,
    val title: String,
    val difficulty: String,
    val category: String,
    val description: String,
    val starterCode: String,
    val hint: String
)

object ChallengeLibrary {
    val challenges = listOf(
        PythonChallenge(
            id = "c1",
            title = "Hello, World & Variables",
            difficulty = "Easy",
            category = "Basics",
            description = "Create two variables `name` and `age`, then print a greeting in format: 'Hello, my name is Alice and I am 20 years old.'",
            starterCode = "# Step 1: Create variables name and age\nname = \"Alice\"\nage = 20\n\n# Step 2: Print using f-string or formatting\nprint(f\"Hello, my name is {name} and I am {age} years old.\")\n",
            hint = "Use formatted strings: print(f\"Hello {name}!\")"
        ),
        PythonChallenge(
            id = "c2",
            title = "Even or Odd Checker",
            difficulty = "Easy",
            category = "Logic",
            description = "Write a function `is_even(num)` that returns True if a number is even, and False otherwise. Test it with numbers 1 to 10.",
            starterCode = "def is_even(num):\n    # Use modulo operator %\n    return num % 2 == 0\n\nfor i in range(1, 11):\n    status = \"Even\" if is_even(i) else \"Odd\"\n    print(f\"{i} is {status}\")\n",
            hint = "A number is even if num % 2 == 0"
        ),
        PythonChallenge(
            id = "c3",
            title = "Palindrome Checker",
            difficulty = "Medium",
            category = "Strings",
            description = "Check if a given word or phrase is a palindrome (reads same forward and backward, ignoring spaces & case).",
            starterCode = "def is_palindrome(text):\n    clean = text.lower().replace(\" \", \"\")\n    return clean == clean[::-1]\n\nwords = [\"madam\", \"racecar\", \"hello\", \"python\"]\nfor word in words:\n    print(f\"{word}: {'Palindrome' if is_palindrome(word) else 'Not Palindrome'}\")\n",
            hint = "In Python, word[::-1] reverses a string."
        ),
        PythonChallenge(
            id = "c4",
            title = "Draw a Chart with plot()",
            difficulty = "Medium",
            category = "Visual Charts",
            description = "Use the built-in plot() command to draw an interactive graph directly inside PyStudio console!",
            starterCode = "# PyStudio has built-in plot() for visual graphing!\nlabels = [\"Mon\", \"Tue\", \"Wed\", \"Thu\", \"Fri\"]\nsales = [12, 19, 15, 25, 22]\n\n# Call built-in plot function\nplot(labels, sales, \"bar\", \"Weekly Sales Report\")\nprint(\"Graph generated successfully in console above!\")\n",
            hint = "Call plot(x_list, y_list, 'line' or 'bar', 'Title')"
        ),
        PythonChallenge(
            id = "c5",
            title = "Count Vowels in Text",
            difficulty = "Easy",
            category = "Strings",
            description = "Count how many vowels (a, e, i, o, u) exist in a given sentence.",
            starterCode = "def count_vowels(sentence):\n    vowels = \"aeiouAEIOU\"\n    count = sum(1 for char in sentence if char in vowels)\n    return count\n\ntext = \"PyStudio makes Python on Android amazing!\"\nprint(f\"Original text: {text}\")\nprint(f\"Vowel count: {count_vowels(text)}\")\n",
            hint = "Use a list comprehension or generator with sum()"
        ),
        PythonChallenge(
            id = "c6",
            title = "Find Largest and Smallest in List",
            difficulty = "Easy",
            category = "Lists",
            description = "Find max and min values without using built-in max()/min() functions.",
            starterCode = "numbers = [42, 17, 93, 8, 55, 102, 3]\n\nlargest = numbers[0]\nsmallest = numbers[0]\n\nfor n in numbers:\n    if n > largest:\n        largest = n\n    if n < smallest:\n        smallest = n\n\nprint(\"Numbers:\", numbers)\nprint(f\"Largest: {largest}\")\nprint(f\"Smallest: {smallest}\")\n",
            hint = "Iterate through the list and update largest and smallest variables."
        )
    )
}
