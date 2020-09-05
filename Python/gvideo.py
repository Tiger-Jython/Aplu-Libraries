# gvideo.py
# AP
# Version 1.0, Feb 1, 2016


from org.jcodec.api.awt import SequenceEncoder
from java.io import File
from java.awt.image import BufferedImage

resolutions = [(320, 240), (640, 480), (720, 480), (800, 600), (1280, 720), (960,540), (1280, 1024), (1920, 1080)]

class VideoRecorder():
    def __init__(self, obj, filename, res, ulx = 0, uly = 0):
        self.obj = obj
        self.name = ""
        self.img = None
        self.ulx = ulx
        self.uly = uly
        res = res.lower()
        li = res.split("x")
        isOk = True
        try:
            self.width = int(li[0].strip())
            self.height = int(li[1].strip())
        except:
            isOk = False
        if (not isOk) or ((self.width, self.height) not in resolutions):
            print "Illegal resolution:", res
            print "Supported:", VideoRecorder.getSupportedResolutions()
            return
        self.enc = SequenceEncoder(File(File(filename).getAbsolutePath()))
        if str(type(obj)) == "<type 'ch.aplu.turtle.Turtle'>":
            self.name = "Turtle"
            self.traceBuf = obj.getPlayground().getTraceBuffer()
            self.turtleBuf = obj.getPlayground().getTurtleBuffer()
            self.img = BufferedImage(self.width, self.height, BufferedImage.TYPE_INT_ARGB)
            self.g2D = self.img.createGraphics()    
        elif str(type(obj)) == "<class 'gturtle.TurtleFrame'>":
            self.name = "TurtleFrame"
            self.traceBuf = obj.getPlayground().getTraceBuffer()
            self.turtleBuf = obj.getPlayground().getTurtleBuffer()
            self.img = BufferedImage(self.width, self.height, BufferedImage.TYPE_INT_ARGB)
            self.g2D = self.img.createGraphics()    
        elif str(type(obj)) == "<type 'ch.aplu.util.GPanel'>":
            self.name = "GPanel"
            self.img = BufferedImage(self.width, self.height, BufferedImage.TYPE_INT_ARGB)
            self.g2D = self.img.createGraphics()    
            self.panelBuf = obj.getWindow().getBufferedImage()
        elif str(type(obj)) == "<type 'ch.aplu.jgamegrid.GameGrid'>":
            self.name = "GameGrid"
            self.img = BufferedImage(self.width, self.height, BufferedImage.TYPE_INT_ARGB)
            self.g2D = self.img.createGraphics()    

    @staticmethod
    def getSupportedResolutions():
        s = ""
        n = len(resolutions)
        for i in range(n - 1):
            s += str(resolutions[i][0]) + "x" + str(resolutions[i][1]) + ","
        s += str(resolutions[n - 1][0]) + "x" + str(resolutions[n - 1][1])
        return s
                
    def captureImage(self, nb = 1):
        if self.name == "":
            return
        for i in range(nb):
            self.encode()
            
    def encode(self):        
        if self.name == 'Turtle':
            self.g2D.drawImage(self.traceBuf, self.ulx, self.uly, None)
            if not self.obj.isHidden():
                self.g2D.drawImage(self.turtleBuf, self.ulx, self.uly, None)
        elif self.name == 'TurtleFrame':
            self.g2D.drawImage(self.traceBuf, self.ulx, self.uly, None)
            self.g2D.drawImage(self.turtleBuf, self.ulx, self.uly, None)
        elif self.name == 'GPanel':
            self.g2D.drawImage(self.panelBuf, self.ulx, self.uly, None)
        elif self.name == 'GameGrid':
            ggBuf = self.obj.getImage(self.width, self.height)
            self.g2D.drawImage(ggBuf, self.ulx, self.uly, None)
        self.enc.encodeImage(self.img)

    def finish(self):
        if self.name == "":
            return
        try:
            self.enc.finish()
        except:
            pass

