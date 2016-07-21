/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cicomponents.badges.impl;

import com.google.common.io.CharStreams;
import lombok.SneakyThrows;
import org.cicomponents.badges.*;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.osgi.service.component.annotations.Component;

import java.awt.*;
import java.io.InputStreamReader;
import java.util.*;

@Component(property = {"type=shield", "format=svg", "style=flat"})
public class ShieldStyleFlatBadgeMaker implements BadgeMaker {

    @SneakyThrows
    @Override public byte[] make(BadgeProperty... properties) {
        java.util.List<BadgeProperty> props = Arrays.asList(properties);
        BadgeSubject subject = (BadgeSubject) props.stream().filter(prop -> prop instanceof BadgeSubject).findFirst()
                                                  .orElse(BadgeMaker.subject("Unknown"));
        BadgeStatus status = (BadgeStatus) props.stream().filter(prop -> prop instanceof BadgeStatus).findFirst()
                                                   .orElse(BadgeMaker.status("unknown"));
        BadgeColor color = (BadgeColor) props.stream().filter(prop -> prop instanceof BadgeColor).findFirst()
                                               .orElse(BadgeMaker.color("#555"));

        BadgeStatusColor statusColor =
                (BadgeStatusColor) props.stream().filter(prop -> prop instanceof BadgeStatusColor)
                                        .findFirst()
                                        .orElse(BadgeMaker.statusColor("#4c1"));

        int subjectWidth = measureText(subject.getText());
        int statusWidth = measureText(status.getText());

        if (subjectWidth % 2 == 0) { subjectWidth++; }
        if (statusWidth % 2 == 0) { statusWidth++; }

        subjectWidth += 10;
        statusWidth += 10;

        int width = subjectWidth + statusWidth;

        String template = CharStreams
                .toString(new InputStreamReader(getClass().getResourceAsStream("shield-flat.svg.template")));

        String svg = template
                .replaceAll("\\{\\{width\\}\\}", String.valueOf(width))
                .replaceAll("\\{\\{subjectWidth\\}\\}", String.valueOf(subjectWidth))
                .replaceAll("\\{\\{statusWidth\\}\\}", String.valueOf(statusWidth))
                .replaceAll("\\{\\{halfWidth\\}\\}", String.valueOf(subjectWidth/2))
                .replaceAll("\\{\\{quarterWidth\\}\\}", String.valueOf(subjectWidth + (statusWidth/2 - 1)))
                .replaceAll("\\{\\{subject\\}\\}", subject.getText())
                .replaceAll("\\{\\{status\\}\\}", status.getText())
                .replaceAll("\\{\\{color\\}\\}", colorscheme.getOrDefault(color.getText(), color.getText()))
                .replaceAll("\\{\\{alternateColor\\}\\}", colorscheme.getOrDefault(statusColor.getText(), statusColor
                        .getText()));


        return svg.getBytes();
    }

   
    private Font getFont() {
        return Font.decode("Verdana 11");
    }

    private int measureText(String text) {
        SVGGraphics2D g = new SVGGraphics2D(0, 0);
        FontMetrics metrics = g.getFontMetrics(getFont());
        return metrics.stringWidth(text);
    }

    private static Map<String, String> colorscheme = new HashMap<>();

    static {
        colorscheme.put("brightgreen", "#4c1");
        colorscheme.put("green", "#97CA00");
        colorscheme.put("yellow", "#dfb317");
        colorscheme.put("yellowgreen", "#a4a61d");
        colorscheme.put("orange", "#fe7d37");
        colorscheme.put("red", "#e05d44");
        colorscheme.put("blue", "#007ec6");
        colorscheme.put("grey", "#555");
        colorscheme.put("gray", "#555");
        colorscheme.put("lightgrey", "#9f9f9f");
        colorscheme.put("lightgray", "#9f9f9f");
    }
}
