"""Functions used in preparing Guido's gorgeous lasagna.

Learn about Guido, the creator of the Python language:
https://en.wikipedia.org/wiki/Guido_van_Rossum

This is a module docstring, used to describe the functionality
of a module and its functions and/or classes.
"""


EXPECTED_BAKE_TIME = 40 # Expected baking time in minutes for the lasagna.
PREPARATION_TIME = 2 # Time in minutes to prepare a single layer of lasagna.

def bake_time_remaining(elapsed_bake_time):
    """Calculate the bake time remaining.

    :param elapsed_bake_time: int - baking time already elapsed.
    :return: int - remaining bake time (in minutes) derived from 'EXPECTED_BAKE_TIME'.

    Function that takes the actual minutes the lasagna has been in the oven as
    an argument and returns how many minutes the lasagna still needs to bake
    based on the `EXPECTED_BAKE_TIME`.
    """

    return EXPECTED_BAKE_TIME - elapsed_bake_time


def preparation_time_in_minutes(layers):
    """
    Calculate the total preparation time for the lasagna based on the number of layers.

    This function estimates the time needed to prepare the lasagna before baking, assuming a fixed
    time required for each layer.

    :param layers: int - The number of layers in the lasagna.
    :return: int - The total preparation time in minutes.
    """
    return layers * PREPARATION_TIME


def elapsed_time_in_minutes(number_of_layers, elapsed_bake_time):
    """
    Calculate the total time spent on the lasagna recipe, including preparation and baking.

    This function provides a comprehensive estimate of the time invested in cooking the lasagna, 
    combining both the preparation time (dependent on the number of layers) and the time already 
    spent baking in the oven.

    :param number_of_layers: int - The number of layers in the lasagna.
    :param elapsed_bake_time: int - The number of minutes the lasagna has been baking in the oven.
    :return: int - The total time spent on the lasagna, in minutes.
    """
    preparation_time = preparation_time_in_minutes(number_of_layers)
    return preparation_time +  elapsed_bake_time
