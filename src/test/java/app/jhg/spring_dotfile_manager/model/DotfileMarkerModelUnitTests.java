package app.jhg.spring_dotfile_manager.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DotfileMarkerModelUnitTests {
    
    @Test
    public void testFromMarkerFileContents_singleMarker() {
        String markerFileContents = 
"""
name: .zshrc
location: /home/testuser/.zshrc
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(markerFileContents);
        assertEquals(1, markerModels.size());

        DotfileMarkerModel marker = markerModels.get(0);
        assertEquals(".zshrc", marker.name);
        assertEquals(Path.of("/home/testuser/.zshrc"), marker.location);
    }

    @Test
    public void testFromMarkerFileContents_multipleMarkers() {
        String markerFileContents =
"""
---
name: .zshrc
location: /home/testuser/.zshrc
---
name: .vimrc
location: /home/testuser/.vimrc
""";
        
        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(markerFileContents);
        assertEquals(2, markerModels.size());

        DotfileMarkerModel zshrcMarker = markerModels.get(0);
        assertEquals(".zshrc", zshrcMarker.name);
        assertEquals(Path.of("/home/testuser/.zshrc"), zshrcMarker.location);

        DotfileMarkerModel vimrcMarker = markerModels.get(1);
        assertEquals(".vimrc", vimrcMarker.name);
        assertEquals(Path.of("/home/testuser/.vimrc"), vimrcMarker.location);
    }

    @Test
    public void testFromMarkerFileContents_documentSplitInMiddle() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
---
name: .vimrc
location: /home/testuser/.vimrc
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(markerFileContents);
        assertEquals(2, markerModels.size());

        DotfileMarkerModel zshrcMarker = markerModels.get(0);
        assertEquals(".zshrc", zshrcMarker.name);
        assertEquals(Path.of("/home/testuser/.zshrc"), zshrcMarker.location);

        DotfileMarkerModel vimrcMarker = markerModels.get(1);
        assertEquals(".vimrc", vimrcMarker.name);
        assertEquals(Path.of("/home/testuser/.vimrc"), vimrcMarker.location);
    }

    @Test
    public void testFromMarkerFileContents_documentSplitAtEnd() {
        String markerFileContents =
"""
---
name: .zshrc
location: /home/testuser/.zshrc
---
name: .vimrc
location: /home/testuser/.vimrc
---                
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(markerFileContents);
        assertEquals(2, markerModels.size());

        DotfileMarkerModel zshrcMarker = markerModels.get(0);
        assertEquals(".zshrc", zshrcMarker.name);
        assertEquals(Path.of("/home/testuser/.zshrc"), zshrcMarker.location);

        DotfileMarkerModel vimrcMarker = markerModels.get(1);
        assertEquals(".vimrc", vimrcMarker.name);
        assertEquals(Path.of("/home/testuser/.vimrc"), vimrcMarker.location);
    }
}
