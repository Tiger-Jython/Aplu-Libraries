from neopixel import *
import gc
from microbit import pin2


_nbLeds = 24
_np = NeoPixel(pin2, _nbLeds)

# Fill all pixels with the same color.
# The color is specified by giving values between 0 and 255 for red, green and blue
def fill(red, green, blue):
  for i in range(_nbLeds):
    _np[i] = (red, green, blue)
  _np.show()

# Set the specified pixel to the specified color.
# The pixel is specified by a number between 0 and 23.
# The color is specified by giving values between 0 and 255 for red, green and blue
def set_led(pos, red, green, blue):
  _np[pos] = (red, green, blue)
  _np.show()