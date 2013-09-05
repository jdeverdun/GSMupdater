package main;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


public class Updater {

	//Variables contenant les noms des fichiers � charger
	private static final String pathCurrent = File.separator + "Actual.jar";
	private static final String pathNew = File.separator + "New.jar";
	private static final String pathOld = File.separator + "Old.jar";
 
	//Variable contenant le nom du r�p�rtoire courant
	private static final String currentFolder = System.getProperty("user.dir");
	

	//Chemin vers le fichier XML 
	private String xmlPath = "Lien vers fichier XML";
	 
	//Document xml
	private Document xmlDocument = null;
	 
	private ArrayList<String> getVersions(){
		ArrayList<String> versions = new ArrayList<String>();
	 
		try {
			URL xmlUrl = new URL(xmlPath);
	 
			//On ouvre une connections sur la page
			URLConnection urlConnection = xmlUrl.openConnection();
			urlConnection.setUseCaches(false);
	 
			//On se connecte sur cette page
			urlConnection.connect();
	 
			//On r�cup�re le fichier XML sous forme de flux
			InputStream stream = urlConnection.getInputStream();
	 
			SAXBuilder sxb = new SAXBuilder();
	 
			//On cr�e le document xml avec son flux
			try {xmlDocument = sxb.build(stream);
			} catch (JDOMException e) {e.printStackTrace();
			} catch (IOException e) {e.printStackTrace();}
	 
			//On r�cup�re la racine
			Element racine = xmlDocument.getRootElement();
	 
			//On liste toutes les versions
			List listVersions = racine.getChildren("version");
			Iterator iteratorVersions = listVersions.iterator();
	 
			//On parcourt toutes les versions
			while(iteratorVersions.hasNext()){
				Element version = (Element)iteratorVersions.next();
	 
				Element elementNom = version.getChild("nom");
	 
				versions.add(elementNom.getText());
			}
	 
			//On trie la liste
			Collections.sort(versions);
	 
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	 
		return versions;
	}

	private void downloadFile(String filePath, String destination) { 
		URLConnection connection = null;
		InputStream is = null;
		FileOutputStream destinationFile = null;
	 
		try { 
			//On cr�e l'URL
	        URL url = new URL(filePath);
	 
			//On cr�e une connection vers cet URL
			connection = url.openConnection( );
	 
			//On r�cup�re la taille du fichier
			int length = connection.getContentLength();
	 
			//Si le fichier est inexistant, on lance une exception
			if(length == -1){
				throw new IOException("Fichier vide");
	       	}
	 
			//On r�cup�re le stream du fichier
			is = new BufferedInputStream(connection.getInputStream());
	 
			//On pr�pare le tableau de bits pour les donn�es du fichier
			byte[] data = new byte[length];
	 
			//On d�clare les variables pour se retrouver dans la lecture du fichier
			int currentBit = 0;
			int deplacement = 0;
	 
			//Tant que l'on n'est pas � la fin du fichier, on r�cup�re des donn�es
			while(deplacement < length){
				currentBit = is.read(data, deplacement, data.length-deplacement);	
				if(currentBit == -1)break;	
				deplacement += currentBit;
			}
	 
			//Si on n'est pas arriv� � la fin du fichier, on lance une exception
			if(deplacement != length){
				throw new IOException("Le fichier n'a pas �t� lu en entier (seulement " 
					+ deplacement + " sur " + length + ")");
			}		
	 
			//On cr�e un stream sortant vers la destination
			destinationFile = new FileOutputStream(destination); 
	 
			//On �crit les donn�es du fichier dans ce stream
			destinationFile.write(data);
	 
			//On vide le tampon et on ferme le stream
			destinationFile.flush();
	 
	      } catch (MalformedURLException e) { 
	    	  System.err.println("Probl�me avec l'URL : " + filePath); 
	      } catch (IOException e) { 
	        e.printStackTrace();
	      } finally{
	    	  try {
	    		  is.close();
				  destinationFile.close();
	    	  } catch (IOException e) {
	    		  e.printStackTrace();
	    	  }
	      }
	}

 
	public static void main(String[] args) {
		File current = new File(currentFolder + pathCurrent);
		File newVersion = new File(currentFolder + pathNew);
		File old = new File(currentFolder + pathOld);
 
		//Si une nouvelle version a �t� t�l�charg�e
		if(newVersion.exists()){
			//On renomme la version actuelle (donc la vielle)
			current.renameTo(old);
 
			//On renomme la nouvelle avec le nom de l'ancienne
			newVersion.renameTo(current);
 
			//On supprimme l'ancienne
			old.delete();
 
			try {
				//On lance le nouveau fichier .jar
				Desktop.open(current);
			} catch (DesktopException e) {
				e.printStackTrace();
			}
		//S'il n'y a qu'une version courante et pas de nouvelles
		}else if(current.exists()){
			try {
				//On lance le jar actuel
				Desktop.open(current);
			} catch (DesktopException e) {
				e.printStackTrace();
			}
		//Si aucun fichier n'existe
		}else{
			//On avertit d'un probl�me
			JOptionPane.showMessageDialog(null,"Aucun fichier jar � lancer...");
		}
	}
}
