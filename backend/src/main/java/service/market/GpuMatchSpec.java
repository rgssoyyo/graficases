package com.graficases.backend.service.market;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deriva una query de busqueda y una especificacion de match (modelo + exclusiones)
 * a partir del nombre canonico de la GPU del dataset.
 */
public final class GpuMatchSpec {

    private static final Pattern RTX_GTX = Pattern.compile("\\b(RTX|GTX)\\s*(\\d{3,4})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern RX = Pattern.compile("\\bRX\\s*(\\d{4})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ARC = Pattern.compile("\\b([AB]\\d{3})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MEMORY_GB = Pattern.compile("\\b(\\d{1,2})\\s*GB\\b", Pattern.CASE_INSENSITIVE);

    private final String nombre;
    private final String query;

    private final String modelBaseCompact;
    private final String modelFullCompact;

    private final Integer memoryGb;

    private final boolean hasTi;
    private final boolean hasSuper;
    private final boolean hasXt;
    private final boolean hasXtx;
    private final boolean hasGre;

    private GpuMatchSpec(
            String nombre,
            String query,
            String modelBaseCompact,
            String modelFullCompact,
            Integer memoryGb,
            boolean hasTi,
            boolean hasSuper,
            boolean hasXt,
            boolean hasXtx,
            boolean hasGre
    ) {
        this.nombre = nombre;
        this.query = query;
        this.modelBaseCompact = modelBaseCompact;
        this.modelFullCompact = modelFullCompact;
        this.memoryGb = memoryGb;
        this.hasTi = hasTi;
        this.hasSuper = hasSuper;
        this.hasXt = hasXt;
        this.hasXtx = hasXtx;
        this.hasGre = hasGre;
    }

    public static GpuMatchSpec fromNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre de GPU vacio");
        }

        String normalizedName = normalize(nombre);
        boolean hasTi = normalizedName.contains(" TI ");
        boolean hasSuper = normalizedName.contains(" SUPER");
        boolean hasXtx = normalizedName.contains(" XTX");
        boolean hasXt = normalizedName.contains(" XT") && !hasXtx;
        boolean hasGre = normalizedName.contains(" GRE");

        Integer memoryGb = null;
        Matcher mem = MEMORY_GB.matcher(normalizedName);
        if (mem.find()) {
            try {
                memoryGb = Integer.parseInt(mem.group(1));
            } catch (NumberFormatException ignored) {
                memoryGb = null;
            }
        }

        // Query: quitamos marca "GeForce"/"Radeon" para que la busqueda sea mas robusta.
        String query = nombre
                .replaceFirst("(?i)^\\s*GeForce\\s+", "")
                .replaceFirst("(?i)^\\s*Radeon\\s+", "")
                .trim();

        // Modelo principal en forma compacta para matching robusto.
        String modelBase = null;
        String modelFull = null;

        Matcher rtx = RTX_GTX.matcher(normalizedName);
        if (rtx.find()) {
            String series = rtx.group(1).toUpperCase(Locale.ROOT);
            String number = rtx.group(2);
            modelBase = series + number;
            StringBuilder sb = new StringBuilder(modelBase);
            if (hasTi) {
                sb.append("TI");
            }
            if (hasSuper) {
                sb.append("SUPER");
            }
            modelFull = sb.toString();
        } else {
            Matcher rx = RX.matcher(normalizedName);
            if (rx.find()) {
                String number = rx.group(1);
                modelBase = "RX" + number;
                StringBuilder sb = new StringBuilder(modelBase);
                if (hasXtx) {
                    sb.append("XTX");
                } else if (hasXt) {
                    sb.append("XT");
                } else if (hasGre) {
                    sb.append("GRE");
                }
                modelFull = sb.toString();
            } else {
                Matcher arc = ARC.matcher(normalizedName);
                if (arc.find()) {
                    String code = arc.group(1).toUpperCase(Locale.ROOT);
                    modelBase = code;
                    modelFull = code;
                } else if (normalizedName.contains("RADEON VII")) {
                    modelBase = "RADEONVII";
                    modelFull = "RADEONVII";
                } else if (normalizedName.contains("VEGA")) {
                    // Ej: "Radeon RX Vega 64" -> VEGA64
                    String compact = toCompact(normalizedName);
                    Matcher vega64 = Pattern.compile("VEGA(\\d{2})").matcher(compact);
                    if (vega64.find()) {
                        modelBase = "VEGA" + vega64.group(1);
                        modelFull = modelBase;
                    }
                } else if (normalizedName.contains("R9") && normalizedName.contains("FURY")) {
                    // R9FURY o R9FURYX
                    String compact = toCompact(normalizedName);
                    modelBase = "R9FURY";
                    modelFull = compact.contains("FURYX") ? "R9FURYX" : "R9FURY";
                } else if (normalizedName.contains("GT 1030")) {
                    modelBase = "GT1030";
                    modelFull = "GT1030";
                } else {
                    // Fallback: intentamos al menos con el ultimo token numerico (ej: "GeForce 210").
                    String compact = toCompact(normalizedName);
                    Matcher digits = Pattern.compile("(\\d{3,4})").matcher(compact);
                    List<String> nums = new ArrayList<>();
                    while (digits.find()) {
                        nums.add(digits.group(1));
                    }
                    if (!nums.isEmpty()) {
                        String last = nums.getLast();
                        modelBase = last;
                        modelFull = last;
                    }
                }
            }
        }

        return new GpuMatchSpec(
                nombre,
                query,
                modelBase,
                modelFull,
                memoryGb,
                hasTi,
                hasSuper,
                hasXt,
                hasXtx,
                hasGre
        );
    }

    public String nombre() {
        return nombre;
    }

    public String query() {
        return query;
    }

    public Integer memoryGb() {
        return memoryGb;
    }

    public boolean matchesTitle(String title) {
        if (title == null || title.isBlank()) {
            return false;
        }

        String compactTitle = toCompact(title);
        if (modelFullCompact != null && !compactTitle.contains(modelFullCompact)) {
            return false;
        }

        if (modelBaseCompact != null) {
            // Evitar confundir "base" con variantes (SUPER/TI/XTX/XT/GRE) cuando el dataset NO la tiene.
            if (!hasTi && compactTitle.contains(modelBaseCompact + "TI")) {
                return false;
            }
            if (!hasSuper && compactTitle.contains(modelBaseCompact + "SUPER")) {
                return false;
            }

            if (!hasXtx && compactTitle.contains(modelBaseCompact + "XTX")) {
                return false;
            }
            // Nota: XTX contiene "XT" como prefijo, asi que solo vetamos XT cuando NO es XTX.
            if (!hasXt && !hasXtx && compactTitle.contains(modelBaseCompact + "XT")) {
                return false;
            }
            if (!hasGre && compactTitle.contains(modelBaseCompact + "GRE")) {
                return false;
            }
        }

        if (memoryGb != null) {
            String gb = memoryGb + "GB";
            String g = memoryGb + "G";
            if (!compactTitle.contains(gb) && !compactTitle.contains(g)) {
                return false;
            }
        }

        return true;
    }

    private static String normalize(String input) {
        String noAccents = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String cleaned = noAccents
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", " ")
                .toUpperCase(Locale.ROOT)
                .trim();
        return " " + cleaned + " ";
    }

    static String toCompact(String input) {
        if (input == null) {
            return "";
        }
        String noAccents = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccents
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", "")
                .toUpperCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GpuMatchSpec that)) return false;
        return Objects.equals(nombre, that.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }
}
