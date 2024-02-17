""" excercism armstrong-numbers exercise solution 

An Armstrong number is a number that is the sum of its own digits each raised to the power of the number of digits.
"""


def is_armstrong_number(number):
    """checks if a number is an armstrong number

    :param number: int - the number to check
    :return bool - result of the check
    """

    # Convert the number to a string to iterate over each digit.
    digits = str(number)
    num_digits = len(digits)

    # Calculate the sum of each digit raised to the power of the number of digits.
    sum_of_powers = sum([int(digit) ** num_digits for digit in digits])

    # Check if the sum of powers is equal to number.
    return sum_of_powers == number
