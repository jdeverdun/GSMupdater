package gsm.update;

import java.awt.Desktop;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;



public class Updater {
 
	//Variable contenant le nom du répértoire courant
	private static final String currentFolder = System.getProperty("user.dir");

	//Chemin vers le fichier XML 
	private String xmlPath = "http://120.40.30.110:8888/XellSior/update.xml";
	 
	//Document xml
	private Document xmlDocument = null;
	 
	public ArrayList<String> getVersions(){
		ArrayList<String> versions = new ArrayList<String>();
	 
		try {
			URL xmlUrl = new URL(xmlPath);
	 
			//On ouvre une connections sur la page
			URLConnection urlConnection = xmlUrl.openConnection();
			urlConnection.setUseCaches(false);
	 
			//On se connecte sur cette page
			urlConnection.connect();
	 
			//On récupère le fichier XML sous forme de flux
			InputStream stream = urlConnection.getInputStream();
	 
			SAXBuilder sxb = new SAXBuilder();
	 
			//On crée le document xml avec son flux
			try {xmlDocument = sxb.build(stream);
			} catch (JDOMException e) {e.printStackTrace();
			} catch (IOException e) {e.printStackTrace();}
	 
			//On récupère la racine
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
		JFrame.setDefaultLookAndFeelDecorated(true);
		try {
	          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
	        } catch (Exception e) {
	          System.out.println("Substance Graphite failed to initialize");
	        }
		UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
		Image img = new ImageIcon(this.getClass().getResource("/gsm/images/logo32.png")).getImage();
		JFrame jf = new JFrame("Downloading");

		try { 
			//On crée l'URL
	        URL url = new URL(filePath);
	 
			//On crée une connection vers cet URL
			connection = url.openConnection( );
	 
			//On récupère la taille du fichier
			int length = connection.getContentLength();
			JProgressBar progressBar = new JProgressBar(0, length);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			jf.add(progressBar);
			jf.setSize(200, 70);
			jf.setLocationRelativeTo(null);
			jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			jf.setVisible(true);
			//Si le fichier est inexistant, on lance une exception
			if(length == -1){
				throw new IOException("Fichier vide");
	       	}
	 
			//On récupère le stream du fichier
			is = new BufferedInputStream(connection.getInputStream());
	 
			//On prépare le tableau de bits pour les données du fichier
			byte[] data = new byte[length];
	 
			//On déclare les variables pour se retrouver dans la lecture du fichier
			int currentBit = 0;
			int deplacement = 0;
	 
			//Tant que l'on n'est pas à la fin du fichier, on récupère des données
			while(deplacement < length){
				currentBit = is.read(data, deplacement, data.length-deplacement);	
				if(currentBit == -1)break;	
				deplacement += currentBit;
				progressBar.setValue(deplacement);
			}
	 
			//Si on n'est pas arrivé à la fin du fichier, on lance une exception
			if(deplacement != length){
				throw new IOException("Le fichier n'a pas été lu en entier (seulement " 
					+ deplacement + " sur " + length + ")");
			}		
	 
			//On crée un stream sortant vers la destination
			destinationFile = new FileOutputStream(destination); 
	 
			//On écrit les données du fichier dans ce stream
			destinationFile.write(data);
	 
			//On vide le tampon et on ferme le stream
			destinationFile.flush();
			jf.dispose();
	      } catch (MalformedURLException e) { 
	    	  System.err.println("Problème avec l'URL : " + filePath); 
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


	/**
	 * Met a jours panther.jar avec la version demandee et relance le fichier jar present au meme emplacement que l'updater
	 * @param versionChoisie
	 * @param jarfile
	 */
	public void update(String versionChoisie,String jarfile){
		ArrayList<String> versions = getVersions();
			//Si la dernière version n'est pas la même que l'actuelle
			//if(!versions.get(versions.size() - 1).equals(version)){

		//S'il veut la télécharger
		if(versionChoisie != ""){					
			Element racine = xmlDocument.getRootElement();

			//On liste toutes les versions
			List listVersions = racine.getChildren("version");
			Iterator iteratorVersions = listVersions.iterator();

			//On parcourt toutes les versions
			while(iteratorVersions.hasNext()){
				Element version = (Element)iteratorVersions.next();

				Element elementNom = version.getChild("nom");

				//Si c'est la bonne version, on télécharge tous ses fichiers
				if(elementNom.getText().equals((String)versionChoisie)){
					Element elementFiles = version.getChild("files");

					//On liste tous les fichiers d'une version
					List listFiles = elementFiles.getChildren("file");
					Iterator iteratorFiles = listFiles.iterator();

					//On parcours chaque fichier de la version
					while(iteratorFiles.hasNext()){
						Element file = (Element)iteratorFiles.next();

						//On télécharge le fichier
						downloadFile(file.getChildText("url"),currentFolder + 
								File.separator + file.getChildText("destination"));
					}

					break;
				}
			}
			
			// --------- Affichage du resultat -------------
			JFrame.setDefaultLookAndFeelDecorated(true);
			try {
		          UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
		        } catch (Exception e) {
		          System.out.println("Substance Graphite failed to initialize");
		        }
			UIManager.put(SubstanceLookAndFeel.WINDOW_ROUNDED_CORNERS, Boolean.FALSE);
			Image img = new ImageIcon(this.getClass().getResource("/gsm/images/logo32.png")).getImage();
			
		    JFrame f = new JFrame(""); // juste pour afficher l'icone du l'optionPane
		    f.setIconImage(img);
		    JDialog.setDefaultLookAndFeelDecorated(true);
			JOptionPane.showMessageDialog(f,"Last version downloaded successfully.");

			
			// ------------ lancement du programme ------------
			Path cdir = null;
			try {
				cdir = Paths.get(new File(".").getCanonicalPath());
			} catch (final IOException e1) {
				e1.printStackTrace();
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						JDialog.setDefaultLookAndFeelDecorated(true);
						JOptionPane.showMessageDialog(null,
							    "Error during update ("+e1.toString()+")",
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
					}
				});
			}
			if(jarfile != null){
				ProcessBuilder pb = new ProcessBuilder("java", "-jar", cdir+File.separator+jarfile);
				pb.directory(cdir.toFile());
				try {
					Process p = pb.start();
					f.dispose();
					System.exit(0);
				} catch (final IOException e) {
					e.printStackTrace();
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							JDialog.setDefaultLookAndFeelDecorated(true);
							JOptionPane.showMessageDialog(null,
								    "Error during update ("+e.toString()+")",
								    "Error",
								    JOptionPane.ERROR_MESSAGE);
						}
					});
				}
			}
		}
	}

	public static void main(String[] args) {
		Updater upd=new Updater();
		upd.update(args[0],args[1]);
	}
}
