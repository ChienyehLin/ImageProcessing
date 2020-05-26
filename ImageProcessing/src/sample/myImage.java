package sample;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class myImage {
    BufferedImage image;
    int [][]RGB;
    int [][]palette;
    int PCX_version_number;
    int manufacturer;
    int Encoding;
    int BitsPerPixel;
    int Xmin,Ymin,Xmax,Ymax;
    int Hdpi;
    int Vdpi;
    int BytesPerLine;
    int PaletteInfo;
    int VscreenSize;
    int HscreenSize;
    int Reserved;
    int NPlanes;
    public myImage(byte[] bytes) throws  Exception
    {

        this.manufacturer=bytes[0];
        this.PCX_version_number = bytes[1];
        this.BitsPerPixel=bytes[3];
        byte []temp = new byte[2];
        this.Encoding =bytes[2];
        temp[0]=bytes[69];
        temp[1]=bytes[68];
        this.Xmin = readShort(bytes,4);
        this.Ymin = readShort(bytes,6);
        this.Xmax = readShort(bytes,8);
        this.Ymax = readShort(bytes,10);
        this.Hdpi = readShort(bytes,12);
        this.Vdpi = readShort(bytes,14);
        this.Reserved = bytes[64];
        this.NPlanes = bytes[65];
        this.BytesPerLine =readShort(bytes,66);
        this.PaletteInfo =readShort(bytes,68);
        this.HscreenSize =readShort(bytes,70);
        this.VscreenSize =readShort(bytes,72);
    }
    public static short readShort(byte[] data, int offset) {
        return (short) (  ((data[offset + 1] <<8))|((data[offset] &0xff)));
    }
    public int getUnsignedByte (short data){      //将data字节型数据转换为0~65535 (0xFFFF 即 WORD)。
        return data&0x0FFFF ;
    }
}
