# cpmike.py

from calliope_mini import pin3, running_time, sleep

_click_time = running_time()

def isClicked(level = 10, rearm_time = 500):
    global _click_time
    if running_time() - _click_time < rearm_time:
        sleep(10)
        return False
    v = pin3.read_analog()
    if v < 518 - level:
        _click_time = running_time()
        return True
    return False
