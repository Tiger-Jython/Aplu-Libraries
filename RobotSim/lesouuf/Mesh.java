
package ch.aplu.robotsim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;


/*
 * Pour définir le contour d'un objet, nécessaire pour le capteur ultrason.
 * 
 *  Recherche des contours d'un objet convexe dans une image dont le fond est transparent dans le sens horaire
 *  Fonctionne dans certains cas d'objets concaves
 *  Nombre de points du contour: 2^n avec n>1 
 *  
 *  
 *  
 *  Etape 1: recherche du rectangle qui englobe l'objet. On obtient 4 points de l'objet
 *  Etape 2: on recherche le dernier point de l'objet situé sur la médiatrice de deux points consécutifs A et B de l'objet 
 *  Etape 3: si le  milieu de  appartient à l'objet, on parcourt la médiatrice dans le sens anti horaire (convexité)
 *  		 sinon dans le sens horaire (concavité)
 Auteur: D.LESOUEF 2015/09/01
 */
public class Mesh
{
  Mesh()
  {
  }

  BufferedImage target;
//	int colorBackground;// couleur du fond de l'image
  int N = 4; //nombre de points du contour
  Point[] carre = new Point[4];
  Point[] mesh = new Point[4];
  Point[] m;

  BufferedImage readImage(String nameImage)
  {// ouverture du fichier image et récupération de la couleur des pixels dans un tableau à deux dimensions
    BufferedImage image = null;
    try
    {
      image = ImageIO.read(new File(nameImage));
      carre[0] = new Point(0, 0);
      carre[1] = new Point(image.getWidth() - 1, 0);
      carre[2] = new Point(carre[1].x, image.getHeight() - 1);
      carre[3] = new Point(0, carre[2].y);
    }
    catch (IOException e)
    {
      // gestion des erreurs d'ouverture du fichier
      e.printStackTrace();
    }
    return image;
  }

  public boolean isAlpha(int x, int y)
  {
    return (target.getRGB(x, y) & 0xFF000000) == 0xFF000000;
  }

  public boolean isNotAlpha(int x, int y)
  {
    return (target.getRGB(x, y) & 0xFF000000) != 0xFF000000;
  }

  void rechercheH0()
  {
		//recherche du premier point de l'objet en haut à gauche de l'image
    // balayage de l'image de la gauche vers la droite du haut vers le bas

    int i = 0, j = 0,
      endi = target.getWidth(), endj = target.getHeight();
    boolean trouve = false;
    while (!trouve && j < endj)
    {
      i = 0;
      while (!trouve && i < endi)
      {
        //trouve=(target.getRGB(i,j)!=colorBackground);
        trouve = isNotAlpha(i, j);
        i++;
      }
      j++;
    }
    mesh[0] = new Point(i, j);
  }

  void rechercheV0()
  {
			// recherche du premier point de l'objet en haut à droite de l'image
    // balayage de l'image du haut vers le bas de la droite vers la gauche 

    int i = target.getWidth() - 1, j = 0, endj = target.getHeight();
    boolean trouve = false;
    while (!trouve && i > -1)
    {
      j = 0;
      while (!trouve && j < endj)
      {
        //trouve=(target.getRGB(i,j)!=colorBackground);
        trouve = isNotAlpha(i, j);
        j++;
      }
      i--;

    }
    mesh[1] = new Point(i, j);
  }

  void rechercheH1()
  {
			//recherche du premier point de l'objet en bas à droite de l'image
    // balayage de l'image de la droite vers la gauche du bas vers le haut

    int i = target.getWidth() - 1, j = target.getHeight() - 1, endi = target.getWidth(), endj = target.getHeight();
    boolean trouve = false;
    while (!trouve && j > -1)
    {
      i = endi - 1;
      while (!trouve && i > -1)
      {
        //trouve=(target.getRGB(i,j)!=colorBackground);
        trouve = isNotAlpha(i, j);
        i--;
      }
      j--;
    }
    mesh[2] = new Point(i, j);
  }

  void rechercheV1()
  {
			// recherche du premier point de l'objet en bas à gauche de l'image
    // balayage de l'image du bas vers le haut de la gauche vers la droite 

    int i = 0, j = 0, endj = target.getHeight(), endi = target.getWidth();
    boolean trouve = false;
    while (!trouve && i < endi)
    {
      j = endj - 1;
      while (!trouve && j > -1)
      {

        //trouve=(target.getRGB(i,j)!=colorBackground);
        trouve = isNotAlpha(i, j);
        j--;
      }
      i++;
    }
    mesh[3] = new Point(i, j);
  }

  Point getPoint(Point A, Point B)
  {
    boolean b = true;

    int xi = (A.x + B.x) / 2, yi = (A.y + B.y) / 2;
    int k = 0, signe = 1;

    double my = B.x - A.x, mx = A.y - B.y, m = signe * (my * 1.0) / mx, norme = Math.sqrt(mx * mx + my * my), x, y, x0, y0;
    my = my / norme;
    mx = mx / norme;
    x = xi;
    y = yi;
    if (isNotAlpha(xi, yi)/*target.getRGB(xi,yi)!=colorBackground*/)
    {

      do
      {
        k++;
        x0 = x;
        y0 = y;
        x = x - mx;
        y = y - my;

        if (x < 0)
          break;
        if (y < 0)
          break;
        if (x > target.getWidth() - 1)
          break;
        if (y > target.getHeight() - 1)
          break;
      }
      while (isNotAlpha((int)x, (int)y)/*target.getRGB((int)x,(int)y)!=colorBackground*/);
    }
    else
    {
      do
      {
        k++;
        x0 = x;
        y0 = y;
        x = x + mx;
        y = y + my;
        if (x < 0)
        {
          b = false;
          break;
        }
        if (y < 0)
        {
          b = false;
          break;
        }
        if (x > target.getWidth() - 1)
        {
          b = false;
          break;
        }
        if (y > target.getHeight() - 1)
        {
          b = false;
          break;
        }
      }
      while (isAlpha((int)x, (int)y)/*target.getRGB((int)x,(int)y)==colorBackground*/);
      if (!b)
      {
        x = xi;
        y = yi;
        do
        {
          k++;
          x0 = x;
          y0 = y;
          x = x - mx;
          y = y - my;

          if (x < 0)
            break;
          if (y < 0)
            break;
          if (x > target.getWidth() - 1)
            break;
          if (y > target.getHeight() - 1)
            break;
        }
        while (isAlpha((int)x, (int)y)/*target.getRGB((int)x,(int)y)==colorBackground*/);
      }
    }

    Point m1 = new Point((int)x0, (int)y0);
    return m1;
  }

  public Point[] CreateMeshTarget(String nameImage, Color _colorBackground, int nbPoints)
  {
    //		colorBackground=_colorBackground.getRGB();
    N = nbPoints;

    target = readImage(nameImage);

    rechercheH0();
    rechercheV0();
    rechercheH1();
    rechercheV1();

    m = new Point[N];

    m[0] = mesh[0];
    m[N / 4] = mesh[1];
    m[N / 2] = mesh[2];
    m[(3 * N) / 4] = mesh[3];

    createMesh(0, N / 4);
    createMesh(N / 4, N / 2);
    createMesh(N / 2, (3 * N) / 4);
    createMesh((3 * N) / 4, 0);
    return m;

  }

  public Point[] CreateMeshTarget(BufferedImage _target, Color _colorBackground, int nbPoints)
  {
    //colorBackground=_colorBackground.getRGB();
    N = nbPoints;
    target = _target;

    rechercheH0();
    rechercheV0();
    rechercheH1();
    rechercheV1();

    m = new Point[N];

    m[0] = mesh[0];
    m[N / 4] = mesh[1];
    m[N / 2] = mesh[2];
    m[(3 * N) / 4] = mesh[3];

    createMesh(0, N / 4);
    createMesh(N / 4, N / 2);
    createMesh(N / 2, (3 * N) / 4);
    createMesh((3 * N) / 4, 0);
    return m;

  }

  public Point[] CreateMeshTarget(String nameImage, int _colorBackground, int nbPoints)
  {
    //	colorBackground=_colorBackground;
    N = nbPoints;

    target = readImage(nameImage);

    rechercheH0();
    rechercheV0();
    rechercheH1();
    rechercheV1();

    m = new Point[N];

    m[0] = mesh[0];
    m[N / 4] = mesh[1];
    m[N / 2] = mesh[2];
    m[(3 * N) / 4] = mesh[3];

    createMesh(0, N / 4);
    createMesh(N / 4, N / 2);
    createMesh(N / 2, (3 * N) / 4);
    createMesh((3 * N) / 4, 0);
    return m;
  }

  public Point[] create(String nameImage, int nbPoints)
  {
    N = nbPoints;
    target = readImage(nameImage);
    return createMesh();
  }

  public Point[] createMesh()
  {
    rechercheH0();
    rechercheV0();
    rechercheH1();
    rechercheV1();

    m = new Point[N];

    m[0] = mesh[0];
    m[N / 4] = mesh[1];
    m[N / 2] = mesh[2];
    m[(3 * N) / 4] = mesh[3];

    createMesh(0, N / 4);
    createMesh(N / 4, N / 2);
    createMesh(N / 2, (3 * N) / 4);
    createMesh((3 * N) / 4, 0);
    return m;
  }

  public Point[] create(BufferedImage _target, int nbPoints)
  {
    N = nbPoints;

    target = _target;
    return createMesh();
  }

  public Point[] CreateMeshTarget(BufferedImage _target, int _colorBackground, int nbPoints)
  {
    //colorBackground=_colorBackground;
    N = nbPoints;

    target = _target;

    rechercheH0();
    rechercheV0();
    rechercheH1();
    rechercheV1();

    m = new Point[N];

    m[0] = mesh[0];
    m[N / 4] = mesh[1];
    m[N / 2] = mesh[2];
    m[(3 * N) / 4] = mesh[3];

    createMesh(0, N / 4);
    createMesh(N / 4, N / 2);
    createMesh(N / 2, (3 * N) / 4);
    createMesh((3 * N) / 4, 0);
    return m;
  }

  public void createMesh(int i, int j)
  {
    int k;
    if (j == 0)
      k = (i + N) / 2;
    else
      k = (i + j) / 2;
    m[k] = getPoint(m[i], m[j]);
    if (k - i > 1)
    {
      createMesh(i, k);
      createMesh(k, j);
    }
  }
}
