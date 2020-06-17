package org.grobid.core.utilities;

import java.util.List;
import java.util.Map;

import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.PDFAnnotation;

import java.util.regex.*;

import org.apache.commons.lang3.tuple.Pair;


public class ResultsExtractor {
    private static List<PDFAnnotation> annotations;
    private static Pattern result_regex = Pattern.compile("uri:(theorem\\.(\\w+)|proof)\\.([0-9]+)");

    public ResultsExtractor(List<PDFAnnotation> annotations) {
        this.annotations = annotations;
    }

    public Pair<String, Integer> getKind(LayoutToken token) {
        BoundingBox token_bb = token.getBoundingBox();
        double token_bb_area = token_bb.area();

        for (PDFAnnotation annotation : annotations) {
            Matcher m = result_regex.matcher(annotation.getDestination());
            // check if link is generated from extthm
            if (m.find()) {
                // check link's bounding boxes
                for (BoundingBox b: annotation.getBoundingBoxes()) {
                    // check if bounding box intersects with token.
                    if (b.intersect(token_bb) && b.boundingBoxIntersection(token_bb).area()/token_bb_area >= 0.5) {

                        Integer result_n = Integer.parseInt(m.group(3));

                        if (m.group(1).equals("proof")) {
                            return Pair.of("proof", result_n);
                        } else {
                            return Pair.of(m.group(2), result_n);
                        }
                    }
                }
            }
        }
        return Pair.of("text", 0);
    }
}