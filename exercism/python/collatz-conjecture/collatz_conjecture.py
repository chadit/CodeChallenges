""" exercism Collatz Conjecture. """


def steps(number):
    """Calculate how many steps in took to reach 1.

    :param number: int - starting with any positive integer
    :return: int - count of steps needed to reach 1.
    """

    if type(number) != int or number <= 0:
        raise ValueError("Only positive integers are allowed")

    counter = 0

    while number != 1:
        counter += 1

        # check if number is even
        if number % 2 == 0:
            number /= 2
        else:
            number = number * 3 + 1

    return counter
