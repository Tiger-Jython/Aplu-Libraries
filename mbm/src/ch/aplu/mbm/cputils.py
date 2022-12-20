# cputils.py
# V1.1, Dec 5, 2018
# Additional classes / global functions for Calliope


def cat(filename):
    with open(filename) as f:
        line = f.readline()
        while line:
            print(line[:-1])
            line = f.readline()


from math import asin, atan2, sqrt, degrees

def getPitch(a):
    pitch = atan2(a[1], a[2])
    return int(degrees(pitch))

def getRoll(a):
    anorm = sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2])
    roll = asin(a[0] / anorm)
    return int(degrees(roll))