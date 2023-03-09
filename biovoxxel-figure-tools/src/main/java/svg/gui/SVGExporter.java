package svg.gui;

import java.io.File;

import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.WindowManager;
import ij.process.LUT;
import svg.exporter.objects.SVG_Object_Factory;
import svg.utilities.SvgUtilities;


/**
 * 
 * @author Jan Brocher / BioVoxxel
 *
 */
@Plugin(type = Command.class, menuPath="Plugins>BioVoxxel Figure Tools>Export SVG")
public class SVGExporter extends DynamicCommand {
	

	@Parameter
	ImagePlus imp;

	@Parameter(label = "File name", initializer = "getImageName", required = true, persist = false)
	String fileName = "NoName.svg";
	
	@Parameter(label = "Target folder", required = true, style = "directory")
	File folder;

	@Parameter(label = "Export channels", choices = {"None", "Color", "Grayscale", "Color (no overlays)", "Grayscale (no overlays)"})
	String exportChannelsSeparately = "None";
	
	@Parameter(label = "Interpolate ROIs", min = "0.0", persist = true, description = "if 0.0 polygon ROIs will not be interpolated")
	Double interpolationRange = 0.0;
	
	public void run() {
			
		SVG_Object_Factory.saveImageAndOverlaysAsSVG(imp, createSVGFile(), interpolationRange, true);
		
		if (!exportChannelsSeparately.equalsIgnoreCase("none")) {
			exportIndividualChannels();			
		}
		
	}

	private void exportIndividualChannels() {
		
		LUT gray = SvgUtilities.getGrayLut();
		
		for (int channel = 1; channel <= imp.getNChannels(); channel++) {
			imp.setC(channel);
			fileName = "C" + channel + "-" + imp.getTitle();
			ImagePlus currentChannel = new ImagePlus(fileName, imp.getProcessor());
			if (exportChannelsSeparately.contains("Grayscale")) {
				currentChannel.setLut(gray);					
			}
			if (!exportChannelsSeparately.contains("(no overlays)")) {
				currentChannel.setOverlay(imp.getOverlay());					
			}
			System.out.println("fileName = " + fileName);
			SVG_Object_Factory.saveImageAndOverlaysAsSVG(currentChannel, createSVGFile(), interpolationRange, true);
		}
	}	
	
	private File createSVGFile() {
		if (fileName.equals("")) {
			fileName = "ImageWithoutFileName.svg";
		}
		
		String folderPath = folder.getAbsolutePath();
		if (!folderPath.endsWith(File.separator)) {
			folderPath += File.separator;
		}
		
		String finalFilePath = folderPath + fileName;
		File outputFile = new File(finalFilePath);
		
		return outputFile;
	}
	
	
	private String getSVGFileName() {		
		String fileName = "";
		String imageTitle = WindowManager.getCurrentImage().getTitle();
		int fileExtensionSeparatorPosition = imageTitle.lastIndexOf(".");
		if (fileExtensionSeparatorPosition != -1) {
			fileName = imageTitle.substring(0, fileExtensionSeparatorPosition) + ".svg";
		} else {
			fileName = imageTitle + ".svg";
		}
		return fileName;
	}
	
	
	@SuppressWarnings("unused")
	private void getImageName() {
		
		final MutableModuleItem<String> mutableFileName = getInfo().getMutableInput("fileName", String.class);

		mutableFileName.setValue(this, getSVGFileName());
		
	}	
}