# soundsystem.py
# AP
# Version 1.13, Nov 2, 2018

from ch.aplu.util import SoundPlayer, SoundPlayerExt, SoundRecorder, SoundPlayerListener
from ch.aplu.util.Waveform import SineWave, SquareWave, TriangleWave, SawtoothWave, ChirpWave
from javax.sound.sampled import AudioFormat
from inspect import isfunction
import os
from enum import enum



# ------------------------- MaryTTS -----------------------------
from java.util import Locale

def importJar(jarFile):
   if not os.path.exists(jarFile):
      raise IOError("Can't import " + jarFile)
   from java.net import URL, URLClassLoader
   from java.lang import ClassLoader
   from java.io import File
   m = URLClassLoader.getDeclaredMethod("addURL", [URL])
   m.accessible = 1
   m.invoke(ClassLoader.getSystemClassLoader(), [File(jarFile).toURL()])

__maryhome = ""


schu = ["", "un ", "deux ", "trois ", "quatre ", "cinq ", "six ", "sept ", "huit ", "neuf "]
schud = ["dix ", "onze ", "douze ", "treize ", "quatorze ", "quinze ", "seize ", "dix sept ", "dix huit ", "dix neuf "]
schd = ["", "dix ", "vingt ", "trente ", "quarante ", "cinquante ", "soixante ", "soixante ", "quatre vingt ",
        "quatre vingt "]

# code from pecten with thanks to the author (modified)
def numberToText(nombre):
    s = ''
    reste = nombre
    i = 1000000000
    while i > 0:
        y = reste // i
        if y != 0:
            centaine = y // 100
            dizaine = (y - centaine * 100) // 10
            unite = y - centaine * 100 - dizaine * 10
            if centaine == 1:
                s += "cent "
            elif centaine != 0:
                s += schu[centaine] + "cent "
                if dizaine == 0 and unite == 0: s = s[:-1] + "s "
            if dizaine not in [0, 1]: s += schd[dizaine]
            if unite == 0:
                if dizaine in [1, 7, 9]:
                    s += "dix "
                elif dizaine == 8:
                    s = s[:-1] + "s "
            elif unite == 1:
                if dizaine in [1, 9]:
                    s += "once "
                elif dizaine == 7:
                    s += "et once "
                elif dizaine in [2, 3, 4, 5, 6]:
                    s += "et un "
                elif dizaine in [0, 8]:
                    s += "un "
            elif unite in [2, 3, 4, 5, 6, 7, 8, 9]:
                if dizaine in [1, 7, 9]:
                    s += schud[unite]
                else:
                    s += schu[unite]
            if i == 1000000000:
                if y > 1:
                    s += "milliards "
                else:
                    s += "milliards "
            if i == 1000000:
                if y > 1:
                    s += "millions "
                else:
                    s += "millions "
            if i == 1000:
                s += "mille "
        reste -= y * i
        dix = False
        i //= 1000;
    if len(s) == 0: s += "zero "
    s = s[:-1]  # remove trailing space
    s = s.replace(" ", "-") # insert hyphen instead of space
    if s[0:3] == "un-":  # remove prefix "un-"
        s = s[3:]
    return s

def patchNumbers(s):
    inNumber = False
    isDecimal = False
    isFirst = True
    sep = ""
    out = ""
    i = 0
    size = len(s)
    while i < size:
        if not inNumber and s[i].isdigit() and not isDecimal:
            inNumber = True
            number = s[i]
        elif inNumber and (not s[i].isdigit() or i == size - 1):
            if s[i] == ".":
                isDecimal = True
                sep = " point , "
            elif s[i] == ",":
                isDecimal = True
                sep = " vir culle , "
            inNumber = False
            if i == size - 1 and s[i].isdigit():  # if last char is digit, add it to number
                number += s[i]
            out += numberToText(int(number))
            out += " "
            if i != size - 1: # add next non-digit char to out
                out += s[i]
        else:    
            if inNumber:
                number += s[i]
            elif isDecimal:
                if s[i].isdigit():
                    if isFirst:
                        isFirst = False
                        out += sep
                    out += numberToText(int(s[i])) + " "
                else:
                   isDecimal = False
                   isFirst = True
                   out += s[i]
            else:        
                out += s[i]
        i += 1
    return out

def initTTS(*args):
   global __maryhome, __isTTSInitialized, __isVoiceSelected

   __isTTSInitialized = False
   __isVoiceSelected = False
   
   if len(args) == 0:
      __maryhome = getTigerJythonPath("lib")

   if len(args) == 1:
      __maryhome = args[0] + "/"

   importJar(__maryhome + "marytts-server-5.0-jar-with-dependencies.jar")
   importJar(__maryhome + "marytts-client-5.0-jar-with-dependencies.jar")
   
   __isTTSInitialized = True


def selectVoice(voice):
   global __isVoiceSelected
   __isVoiceSelected = False

   if not __isTTSInitialized:
      raise Exception("Use \"initTextToSound()\" before calling TTS methods.")


   from marytts import LocalMaryInterface
   from marytts import MaryInterface

   global __marytts, __voice

   __voice = voice

   german_woman = "voice-bits1-hsmm-5.0-SNAPSHOT.jar"
   german_man = "voice-bits3-hsmm-5.0-SNAPSHOT.jar"
   us_woman = "voice-dfki-poppy-hsmm-5.1.2.jar"
   us_man = "voice-cmu-bdl-hsmm-5.0-SNAPSHOT.jar"
   french_woman = "voice-enst-camille-hsmm-5.1.2.jar"
   french_man = "voice-pierre-voice-hsmm-5.1-SNAPSHOT.jar"
   italian_woman = "voice-istc-lucia-hsmm-5.1.2.jar"

   lang_german = "marytts-lang-de-5.0.jar"
   lang_us = "marytts-lang-en-5.0.jar"
   lang_french = "marytts-lang-fr-5.1.jar"
   lang_italian = "marytts-lang-it-5.1.jar"

   importJar(__maryhome + lang_german)
   importJar(__maryhome + german_woman)
   importJar(__maryhome + german_man)
   importJar(__maryhome + lang_us)
   importJar(__maryhome + us_man)
   importJar(__maryhome + us_woman)
   importJar(__maryhome + lang_french)
   importJar(__maryhome + french_woman)
   importJar(__maryhome + french_man)
   importJar(__maryhome + lang_italian)
   importJar(__maryhome + italian_woman)
   __marytts = LocalMaryInterface()

   if voice == "german-woman":
      __marytts.setLocale(Locale.GERMAN)
      __marytts.setVoice("bits1-hsmm")
   elif voice == "german-man":
      __marytts.setLocale(Locale.GERMAN)
      __marytts.setVoice("bits3-hsmm")
   elif voice == "english-man":
      __marytts.setLocale(Locale.US)
      __marytts.setVoice("cmu-bdl-hsmm")   
   elif voice == "english-woman":
      __marytts.setLocale(Locale.US)
      __marytts.setVoice("dfki-poppy-hsmm")   
   elif voice == "french-man":
      __marytts.setLocale(Locale.FRENCH)
      __marytts.setVoice("pierre-voice-hsmm")   
   elif voice == "french-woman":
      __marytts.setLocale(Locale.FRENCH)
      __marytts.setVoice("enst-camille-hsmm")   
   elif voice == "italian-woman":
      __marytts.setLocale(Locale.ITALIAN)
      __marytts.setVoice("istc-lucia-hsmm")   
   else:
     raise Exception("Illegal voice for MaryTTS. Supported are: \"german-woman\", \"german-man\",  \"english-man\"")

   __isVoiceSelected = True
   
def generateVoice(text):
   if not __isTTSInitialized:
      raise Exception("Use \"initTextToSound()\" before calling TTS methods.")
   if not __isVoiceSelected:
      raise Exception("Use \"selectVoice()\" before calling generateVoice().")
   if __voice == "french-man" or __voice == "french-woman":
       text = patchNumbers(text)
   return __marytts.generateAudio(text)

# -------------------------------------------------------------

player = None
__playerCounter = 0

recorder = None
__recorderCounter = 0

def isPlayerValid():
   if player == None or __playerCounter != getProgramCounter():
     raise Exception("Use \"openSoundPlayer()\" before calling SoundPlayer methods.")

def isRecorderValid():
   if recorder == None or __recorderCounter != getProgramCounter():
     raise Exception("Use \"openSoundRecorder()\" before calling SoundRecorder methods.")

def do(fun, args):
   y = None
   if len(args) == 0:
      y = fun()
   elif len(args) == 1:
      y = fun(args[0])
   elif len(args) == 2:
      y = fun(args[0], args[1])
   elif len(args) == 3:
      y = fun(args[0], args[1], args[2])
   elif len(args) == 4:
      y = fun(args[0], args[1], args[2], args[3])
   elif len(args) == 5:
      y = fun(args[0], args[1], args[2], args[3], args[4])
   elif len(args) == 6:
      y = fun(args[0], args[1], args[2], args[3], args[4], args[5])
   else:
      raise ValueError("Illegal number of arguments")
   if y != None:
      return y

def openSoundPlayer(*args):
   global __playerCounter
   __playerCounter = getProgramCounter()
   global player
   if player != None:
      player.stop()

   if len(args) == 1:
      if args[0] == None:
         return
      player = SoundPlayer(args[0])

   elif len(args) == 2:
      if args[0] == None:
         return
      if isfunction(args[1]):
         player = SoundPlayer(args[0], notifySoundPlayerStateChange = args[1])
      else:
         player = SoundPlayer(args[0], args[1])

   elif len(args) == 3:
      if args[0] == None:
         return
      player = SoundPlayer(args[0], args[1], notifySoundPlayerStateChange = args[2])

   else:
      raise ValueError("Illegal number of arguments")
   return player

def openMonoPlayer(*args):
   global __playerCounter
   __playerCounter = getProgramCounter()
   global player
   if player != None:
      player.stop()

   if len(args) == 2:
      if args[0] == None:
         return
      player = SoundPlayer(args[0], makeAudioFormatMono(args[1]))

   elif len(args) == 3:
      if args[0] == None:
         return
      player = SoundPlayer(args[0], makeAudioFormatMono(args[1]), notifySoundPlayerStateChange = args[2])

   else:
      raise ValueError("Illegal number of arguments")
   return player

def openStereoPlayer(*args):
   global __playerCounter
   __playerCounter = getProgramCounter()
   global player
   if player != None:
      player.stop()

   if len(args) == 2:
      if args[0] == None:
         return
      player = SoundPlayer(args[0], makeAudioFormatStereo(args[1]))

   elif len(args) == 3:
      if args[0] == None:
         return
      player = SoundPlayer(args[0], makeAudioFormatStereo(args[1]), notifySoundPlayerStateChange = args[2])

   else:
      raise ValueError("Illegal number of arguments")
   return player


def openSoundPlayerMP3(*args):
   global __playerCounter
   __importMP3Libraries()
   __playerCounter = getProgramCounter()
   global player
   if player != None:
      player.stop()

   if len(args) == 1:
      if args[0] == None:
         return
      player = SoundPlayerExt(args[0])

   elif len(args) == 2:
      if args[0] == None:
         return
      if isfunction(args[1]):
         player = SoundPlayerExt(args[0], notifySoundPlayerStateChange = args[1])
      else:
         player = SoundPlayerExt(args[0], args[1])

   elif len(args) == 3:
      if args[0] == None:
         return
      player = SoundPlayerExt(args[0], args[1], notifySoundPlayerStateChange = args[2])

   else:
      raise ValueError("Illegal number of arguments")
   return player

def openMonoPlayerMP3(*args):
   global __playerCounter
   __importMP3Libraries()
   __playerCounter = getProgramCounter()
   global player
   if player != None:
      player.stop()

   if len(args) == 2:
      if args[0] == None:
         return
      player = SoundPlayerExt(args[0], makeAudioFormatMono(args[1]))

   elif len(args) == 3:
      if args[0] == None:
         return
      player = SoundPlayerExt(args[0], makeAudioFormatMono(args[1]), notifySoundPlayerStateChange = args[2])

   else:
      raise ValueError("Illegal number of arguments")
   return player

def openStereoPlayerMP3(*args):
   global __playerCounter
   __importMP3Libraries()
   __playerCounter = getProgramCounter()
   global player
   if player != None:
      player.stop()

   if len(args) == 2:
      if args[0] == None:
         return
      player = SoundPlayerExt(args[0], makeAudioFormatStereo(args[1]))

   elif len(args) == 3:
      if args[0] == None:
         return
      player = SoundPlayerExt(args[0], makeAudioFormatStereo(args[1]), notifySoundPlayerStateChange = args[2])

   else:
      raise ValueError("Illegal number of arguments")
   return player

def openMonoRecorder(*args):
   global __recorderCounter
   __recorderCounter = getProgramCounter()
   global recorder
   if len(args) == 1:
      recorder = SoundRecorder(makeAudioFormatMono(args[0]))
   elif len(args) == 2:
      recorder = SoundRecorder(makeAudioFormatMono(args[0]), args[1])
   else:
      raise ValueError("Illegal number of arguments")
   return recorder

def openStereoRecorder(*args):
   global __recorderCounter
   __recorderCounter = getProgramCounter()
   global recorder
   if len(args) == 1:
      recorder = SoundRecorder(makeAudioFormatStereo(args[0]))
   elif len(args) == 2:
      recorder = SoundRecorder(makeAudioFormatStereo(args[0]), args[1])
   else:
      raise ValueError("Illegal number of arguments")
   return recorder

def addSoundConverter(soundConverter):
   isPlayerValid()
   player.addSoundConverter(soundConverter)

def addSoundPlayerListener(listener):
   isPlayerValid()
   player.addSoundPlayerListener(listener)

def advanceFrames(nbFrames):
   isPlayerValid()
   player.advanceFrames(nbFrames)

def advanceTime(time):
   isPlayerValid()
   player.advanceTime(time)

def blockingPlay():
   isPlayerValid()
   player.blockingPlay()

def delay(time):
   SoundPlayer.delay(time)

def getAudioFormat():
   isPlayerValid()
   return player.getFormat()

def getAvailableMixers():
   return SoundPlayer.getAvailableMixers()

def getCurrentPos():
   isPlayerValid()
   return player.getCurrentPos()

def getCurrentTime():
   isPlayerValid()
   return player.getCurrentTime()

def getFrameRate():
   isPlayerValid()
   return player.getFrameRate()

def getFrameSize():
   isPlayerValid()
   return player.getFrameSize()

def getMixerIndex():
   isPlayerValid()
   player.getMixerIndex()

def getWavMono(filename):
   abspath = os.path.abspath(filename)
   samples = SoundPlayer.getWavMono(abspath)
   if samples == None:  # try to get it from _wav in JAR
      filename = "_" + filename  # append leading _
      samples = SoundPlayer.getWavMono(SoundPlayer.URLfromJAR(filename))
      if samples == None:
         raise Exception("Wav file not found or audio format not supported")
   return samples.tolist()

def getWavStereo(filename):
   abspath = os.path.abspath(filename)
   samples = SoundPlayer.getWavStereo(abspath)
   if samples == None:
      raise Exception("Wav file not found or audio format not supported")
   return samples.tolist()

def getWavInfo(filename):
   abspath = os.path.abspath(filename)
   return SoundPlayer.getWavInfo(abspath)

def getVolume():
   isPlayerValid()
   return player.getVolume()

def isPlaying():
   isPlayerValid()
   return player.isPlaying()

def mute(isMuting):
   isPlayerValid()
   player.mute(isMuting)

def pause():
   isPlayerValid()
   player.pause()

def play():
   isPlayerValid()
   player.play()

def playLoop():
   isPlayerValid()
   player.playLoop()

def replay():
   isPlayerValid()
   player.replay()

def rewindFrames(nbFrames):
   isPlayerValid()
   player.rewindFrames(nbFrames)

def rewindTime(time):
   isPlayerValid()
   player.rewindTime(time)

def setVolume(value):
   isPlayerValid()
   player.setVolume(value)

def stop():
   isPlayerValid()
   player.stop()

def URLfromJAR(audioPath):
   return SoundPlayer.URLfromJAR(audioPath)

def capture():
   isRecorderValid()
   recorder.capture()

def stopCapture():
   isRecorderValid()
   recorder.stopCapture()

def getCapturedBytes():
   isRecorderValid()
   return recorder.getCapturedBytes()

def getCapturedSound():
   data = getCapturedBytes()
   sound = []
   if recorder.getFormat().getChannels() == 1:
     for i in range(len(data) // 2):
        sound.append(data[2 * i] + 256 * data[2 * i + 1])
   else:
     for i in range(len(data) // 4):
        sound.append(data[4 * i] + 256 * data[4 * i + 1])
        sound.append(data[4 * i + 2] + 256 * data[4 * i + 3])
   return sound


def writeWavFile(data, filename):
   abspath = os.path.abspath(filename)
   isRecorderValid()
   return recorder.writeWavFile(data, abspath)

def getAvailableMixers():
   isRecorderValid()
   return recorder.getAvailableMixers()

def makeAudioFormatMono(sampleRate):
   return AudioFormat(sampleRate, 16, 1, True, False)

def makeAudioFormatStereo(sampleRate):
   return AudioFormat(sampleRate, 16, 2, True, False)

# ------------------ FFT --------------------------

def fft(samples, n):
   from org.jtransforms.fft import FloatFFT_1D
   from pl.edu.icm.jlargearrays import FloatLargeArray
   from math import sqrt
   from jarray import array

   a = [0.0] * 2 * n
   for i in range(n):
       a[i] = samples[i]  # fill half of array with input data

   transform = FloatFFT_1D(n)
   ja = array(a, 'f') 
   transform.realForwardFull(ja)

   # Get results
   m = n // 2
   u = [0.0] * m
   for i in range(m):
      u[i] = sqrt(ja[2*i] * ja[2*i] + ja[2*i+1] * ja[2*i+1])

   # normalize        
   z = max(u)
   for i in range(m):
      u[i] = u[i] / z
   return u

def fft_db(samples, n):
   from org.jtransforms.fft import FloatFFT_1D
   from pl.edu.icm.jlargearrays import FloatLargeArray
   from math import sqrt
   from jarray import array

   a = [0.0] * 2 * n
   for i in range(n):
       a[i] = samples[i]  # fill half of array with input data

   transform = FloatFFT_1D(n)
   ja = array(a, 'f') 
   transform.realForwardFull(ja)

   # Get results
   m = n // 2
   u = [0.0] * m
   for i in range(m):
      u[i] = sqrt(ja[2*i] * ja[2*i] + ja[2*i+1] * ja[2*i+1])

   # normalize        
   z = max(u)
   for i in range(m):
      u[i] = 20 * math.log10(u[i] / z)
   return u

def fft_filter(ydata, cutoff, isLowpass):
    # aquidistant samples
    # order 1..len(ydata) // 2
    from org.jtransforms.fft import FloatFFT_1D
    from jarray import array

    n = len(ydata)  # order of FFT
    if cutoff < 0:
        cutoff = 0
    if cutoff > n // 2:
        cutoff = n // 2
    a = [0] * 2 * n
    for i in range(n):
       a[i] = ydata[i]  # fill half of array with input data
    ja = array(a, 'f') 

    # FFT transform
    transform = FloatFFT_1D(n)
    transform.realForward(ja)
    # remove higher frequencies
    if isLowpass:
        for i in range(2 * cutoff, 2 * n):
            ja[i] = 0
    else:
        for i in range(0, 2 * cutoff):
            ja[i] = 0
    # Inverse FFT transform
    transform.realInverse(ja, True)
  
    for i in range(n):    
       ydata[i] = ja[i]


def toAequidistant(xrawdata, yrawdata, deltax):
    xdata = []
    ydata = []
    i = 0
    k = 0
    size = len(xrawdata) - 1
    while k < size:
        x = xrawdata[0] + deltax * i
        while k < size and not (xrawdata[k] <= x and x < xrawdata[k + 1]):
            k += 1
        if k < size:
            x1 = xrawdata[k]
            y1 = yrawdata[k]
            x2 = xrawdata[k + 1]
            y2 = yrawdata[k + 1]
            a = (y2 - y1) / (x2 - x1)
            b = y1 - a * x1
            y = a * x + b
            xdata.append(x)
            ydata.append(y)
        i += 1
    return xdata, ydata

def sine(A, f0, t):
   return int(A * SineWave().f(t, f0))       

def square(A, f0, t):
   return int(A * SquareWave().f(t, f0))       

def sawtooth(A, f0, t):
   return int(A * SawtoothWave().f(t, f0))

def triangle(A, f0, t):
   return int(A * TriangleWave().f(t, f0))      

def chirp(A, f0, t):
   return int(A * ChirpWave().f(t, f0))       


def __importMP3Libraries():
   importJar(getTigerJythonPath("lib") + "tritonus_share.jar")
   importJar(getTigerJythonPath("lib") + "jl1.0.1.jar")
   importJar(getTigerJythonPath("lib") + "mp3spi1.9.4.jar")
