package desmoj.extensions.visualization2d.engine.viewer;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;



/**
 * Swing-application to animate a simulation
 * 
 * @version DESMO-J, Ver. 2.3.4 copyright (c) 2012
 * @author christian.mueller@th-wildau.de
 *         For information about subproject: desmoj.extensions.visualization2d
 *         please have a look at: 
 *         http://www.th-wildau.de/cmueller/Desmo-J/Visualization2d/ 
 * 
 *         Licensed under the Apache License, Version 2.0 (the "License"); you
 *         may not use this file except in compliance with the License. You may
 *         obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied. See the License for the specific language governing
 *         permissions and limitations under the License.
 *
 */
public class ViewerFrame extends JFrame{
	private static final long 		serialVersionUID 	= 1L;
	private ViewerFrame				viewer				= null;
	private ViewerPanel 			viewerPanel 		= null;
	
	/**
	 * starts viewer application
	 * the call must have the form:
	 * 		v = new ViewerFrame(cmdFile, imagePath);
	 * 		v.setLocation(0, 0);
	 * 		v.setSize(800, 500);
	 * 		v.getViewerPanel().setDefaultPath(PATH_DATA, PATH_DATA);
	 * 		v.getViewerPanel().lastCall();
	 * 
	 * @param cmdFile			URL of cmds-file thats opens automaticly, null otherwise 
	 * @param simulationIconDir URL of icon directory
	 * @param locale 			used Locale
	 * @throws IOException 
	 */
	public ViewerFrame(URL cmdFile, URL simulationIconDir, Locale locale) throws IOException{
		super("Viewer: ");
		this.viewer		= this;
		if(locale == null) locale = Locale.ENGLISH;
		this.viewerPanel = new ViewerPanel(cmdFile, simulationIconDir, null, locale);
		
		// setings for ViewerFrame
		this.setTitle("Viewer: "+this.viewerPanel.getViewerName());
		this.setJMenuBar(viewerPanel.createMenueBar(new Exit()));
		
		this.addWindowListener(new WindowClosingAdapter());
		this.viewerPanel.fileOpen();

		JComponent contentPane = (JComponent) this.getContentPane();
		contentPane.setLayout(new GridLayout(1,1));
		contentPane.add(viewerPanel);
		
		this.getLayeredPane().add(ViewerPanel.getInfoPane(), JLayeredPane.DRAG_LAYER);
		
		this.setVisible(true);
	}
	
	public ViewerPanel getViewerPanel(){
		return this.viewerPanel;
	}

	/**
	 * only for testing
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ViewerFrame v = null;
		try{
			v = new ViewerFrame(null, null, null);
			v.setLocation(0, 0);
			v.setSize(800, 500);
		}catch(Exception e){
			v.getViewerPanel().setStatusMessage(e.getMessage());
			e.printStackTrace(ViewerPanel.getLogWriter());
			e.printStackTrace();
		}
	}
	
	/**
	 * ActionListener of Exit-Menue_Item
	 * @author Christian
	 *
	 */
	class Exit implements ActionListener{
		public void actionPerformed(ActionEvent e){
			// SimulationsThread beenden
			if(viewerPanel.getSimulationThread() != null){
				viewerPanel.getSimulationThread().interrupt();
				try {viewerPanel.getSimulationThread().join();
				}catch (InterruptedException ei) {}
				viewerPanel.setSimulationThreadNull();
			}
			viewer.setVisible(false);
			viewer.dispose();
			System.exit(0);
		}
	}

	/**
	 * used by clicking application-closing-button
	 * @author Christian
	 *
	 */
	class WindowClosingAdapter extends WindowAdapter{
		
		public void windowClosing(WindowEvent e){
			// SimulationsThread beenden
			if(viewerPanel.getSimulationThread() != null){
				viewerPanel.getSimulationThread().interrupt();
				try {viewerPanel.getSimulationThread().join();
				}catch (InterruptedException ei) {}
				viewerPanel.setSimulationThreadNull();
			}
			viewer.setVisible(false);
			viewer.dispose();
			//System.exit(0);
		}

	}

}
