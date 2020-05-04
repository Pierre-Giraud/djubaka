package file;

import builders.MediaBuilder;
import exceptions.BadFileExtensionException;
import exceptions.BadMediaTypeException;
import exceptions.InvalidBuilderOperationException;
import media.*;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MediaLoader {
    public static ListMedia loadListFromXPL(String filename, MediaBuilder builder) throws Exception {
        if (!filename.substring(filename.length() - 4).equals(".xpl")) throw new BadFileExtensionException("This file type is not supported");

        InputSource is = new InputSource(new BufferedInputStream(new FileInputStream(filename)));
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(true);
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        HandlerImpl handler = new HandlerImpl(builder);
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);

        xr.parse(is);

        return builder.getList();
    }

    public static Media loadStdMediaFromMediaFile(String filename) throws Exception {
        File file = new File(filename);

        String ext = filename.substring(filename.length() - 4);
        if (!ext.equals(".mta") && !ext.equals(".mtv")) throw new BadMediaTypeException("Media type not supported");

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String duration = reader.readLine();
        if (duration == null || duration.equals("")) throw new InvalidBuilderOperationException("Cannot load : this file does not contain any duration");

        return new StdMedia(Integer.parseInt(duration), filename);
    }

    public static Media loadCompleteMediaFromMediaFile(String filename) throws Exception {
        File file = new File(filename);

        String ext = filename.substring(filename.length() - 4);
        if (!ext.equals(".mta") && !ext.equals(".mtv")) throw new BadMediaTypeException("Media type not supported");

        List<String> data = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        while ((line = reader.readLine()) != null){
            data.add(line);
        }

        if (data.size() < 3) throw new InvalidBuilderOperationException("Cannot load : this file does not contain all the information required");
        else if (data.get(0).equals("")) throw new InvalidBuilderOperationException("Cannot load : this file does not contain any duration");

        if (ext.equals(".mta")) return new StdMusic(Integer.parseInt(data.get(0)), data.get(1), data.get(2));
        return new StdVideo(Integer.parseInt(data.get(0)), data.get(1), data.get(2));
    }
}
