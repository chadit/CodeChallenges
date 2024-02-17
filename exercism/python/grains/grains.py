"""
exercism gains of wheat on a chestboard
"""


def square(number):
    """Calculate the number of grains on a square.

    :param number: int - The number of the square on the chestboard (1 through 64).
    :return: int - The number of grains on the square.
    """

    if number >= 1 and number <= 64:
        return 2 ** (number - 1)
    raise ValueError("square must be between 1 and 64")


def total():
    """Calculate the total number of grains on the chestboard.

    :return: int - The total number of grains on the chestboard.
    """
    return sum(square(n) for n in range(1, 65))
