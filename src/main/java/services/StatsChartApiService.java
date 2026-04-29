package services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class StatsChartApiService {

    private static final String API_URL = "https://quickchart.io/chart";

    public String buildEvolutionChartUrl(List<String> labels, List<Integer> counts, List<Double> quantities) {
        String config = """
                {
                  "type": "line",
                  "data": {
                    "labels": %s,
                    "datasets": [
                      {
                        "label": "Nombre de reponses",
                        "data": %s,
                        "borderColor": "#159461",
                        "backgroundColor": "rgba(21,148,97,0.18)",
                        "fill": false,
                        "tension": 0.25,
                        "yAxisID": "y"
                      },
                      {
                        "label": "Quantite (kg)",
                        "data": %s,
                        "borderColor": "#2D72B8",
                        "backgroundColor": "rgba(45,114,184,0.16)",
                        "fill": false,
                        "tension": 0.25,
                        "yAxisID": "y1"
                      }
                    ]
                  },
                  "options": {
                    "responsive": true,
                    "plugins": {"legend": {"position": "top"}},
                    "scales": {
                      "y": {"beginAtZero": true, "position": "left", "title": {"display": true, "text": "Reponses"}},
                      "y1": {"beginAtZero": true, "position": "right", "grid": {"drawOnChartArea": false}, "title": {"display": true, "text": "Quantite (kg)"}}
                    }
                  }
                }
                """.formatted(jsonStrings(labels), jsonNumbers(counts), jsonDecimals(quantities));
        return chartUrl(config, 690, 330);
    }

    public String buildTopAppelsChartUrl(List<String> labels, List<Integer> counts) {
        String config = """
                {
                  "type": "bar",
                  "data": {
                    "labels": %s,
                    "datasets": [{
                      "label": "Nombre de reponses",
                      "data": %s,
                      "backgroundColor": ["#2FA56F", "#55BE8D", "#81D3AA", "#A8E1C4", "#C8EBD8"],
                      "borderWidth": 0
                    }]
                  },
                  "options": {
                    "plugins": {"legend": {"display": true}},
                    "scales": {"y": {"beginAtZero": true, "ticks": {"precision": 0}}}
                  }
                }
                """.formatted(jsonStrings(labels), jsonNumbers(counts));
        return chartUrl(config, 330, 300);
    }

    public String buildStatusChartUrl(int validated, int pending, int refused) {
        String config = """
                {
                  "type": "doughnut",
                  "data": {
                    "labels": ["Validees", "En attente", "Refusees"],
                    "datasets": [{
                      "data": [%d, %d, %d],
                      "backgroundColor": ["#42BE7A", "#F4C84E", "#E35D64"],
                      "borderColor": "#FFFFFF",
                      "borderWidth": 3
                    }]
                  },
                  "options": {
                    "plugins": {"legend": {"position": "top"}},
                    "cutout": "45%%"
                  }
                }
                """.formatted(validated, pending, refused);
        return chartUrl(config, 330, 300);
    }

    private String chartUrl(String config, int width, int height) {
        String encoded = URLEncoder.encode(config, StandardCharsets.UTF_8);
        return API_URL + "?version=4&width=" + width + "&height=" + height
                + "&backgroundColor=white&format=png&chart=" + encoded;
    }

    private String jsonStrings(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append('"').append(escapeJson(values.get(i))).append('"');
        }
        return json.append(']').toString();
    }

    private String jsonNumbers(List<Integer> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(values.get(i) == null ? 0 : values.get(i));
        }
        return json.append(']').toString();
    }

    private String jsonDecimals(List<Double> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append(String.format(Locale.ROOT, "%.2f", values.get(i) == null ? 0d : values.get(i)));
        }
        return json.append(']').toString();
    }

    private String escapeJson(String value) {
        return (value == null ? "" : value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
