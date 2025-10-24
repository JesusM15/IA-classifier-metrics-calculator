import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void generateConfusionMatrix(String filename, HashSet<String> clases) throws IOException {
        // Convertir el conjunto a lista para indexar
        String[] classList = clases.toArray(new String[0]);
        int n = classList.length;

        // Crear matriz NxN inicializada en 0
        int[][] matrix = new int[n][n];

        // Crear un mapa clase índice
        HashMap<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            indexMap.put(classList[i], i);
        }

        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.toLowerCase().startsWith("id")) continue;

            String[] data = line.split(",");
            String real = data[1].trim();
            String pred = data[2].trim();

            Integer i = indexMap.get(real);
            Integer j = indexMap.get(pred);
            if (i != null && j != null) {
                matrix[i][j]++;
            }
        }
        bufferedReader.close();

        // Guardar matriz en CSV
        FileWriter writer = new FileWriter("MatrizConfusion.csv");
        for (String c : classList) writer.write("," + c);
        writer.write("\n");
        for (int i = 0; i < n; i++) {
            writer.write(classList[i]);
            for (int j = 0; j < n; j++) {
                writer.write("," + matrix[i][j]);
            }
            writer.write("\n");
        }
        writer.close();

    }

    public static String[][] getEvaluation(String clase, String filename, int n, int m) throws IOException {
        String[][] matrix = new String[n][m];

        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line;
        int row = 0;
        while((line = bufferedReader.readLine()) != null){
            if(line.contains("ID,ClaseVerdadera") ) {
                continue;
            }
            String[] data = line.split(",");
            String idx = data[0].trim();
            String real = data[1].trim();
            String pred = data[2].trim();
            String evaluation;
            if (real.equals(clase) && pred.equals(clase))      evaluation = "TP";
            else if (!real.equals(clase) && pred.equals(clase)) evaluation = "FP";
            else if (real.equals(clase) && !pred.equals(clase)) evaluation = "FN";
            else evaluation = "TN";

            matrix[row][0] = idx;
            matrix[row][1] = evaluation;

            row++;

        }

        return matrix;
    }

    public static void generateCSVevaluation(String clase, String[][] table) throws IOException {
        String fileName = "Evaluacion_" + clase + ".csv";

        FileWriter writer = new FileWriter(fileName);
        writer.write("ID,Evaluacion\n");
        for (String[] row : table) {
            if (row[0] == null) continue;
            writer.write(row[0] + "," + row[1] + "\n");
        }
        writer.close();
    }

    public static HashMap<String, Integer> generateClassEvaluations(String clase, String[][] table){
        int tn = 0, tp = 0, fp = 0, fn = 0;
        for(String[] row : table){
            String evaluation = row[1].trim();

            if(evaluation.equals("TN")) tn++;
            else if(evaluation.equals("TP")) tp++;
            else if(evaluation.equals("FP")) fp++;
            else if(evaluation.equals("FN")) fn++;
        }
        HashMap<String, Integer> map = new HashMap<>();
        map.put("tp", tp);
        map.put("fp", fp);
        map.put("fn", fn);
        map.put("tn", tn);
        return map;
    }

    public static HashMap<String, Float> generateClassMetrics(HashMap<String, Integer> evaluations){
        HashMap<String, Float> map = new HashMap<>();

        float tp = evaluations.get("tp");
        float fp = evaluations.get("fp");
        float tn = evaluations.get("tn");
        float fn = evaluations.get("fn");

        float precision = (tp + fp) == 0 ? 0f : tp / (tp + fp);
        float recall    = (tp + fn) == 0 ? 0f : tp / (tp + fn);
        float accuracy  = (tp + tn + fp + fn) == 0 ? 0f : (tp + tn) / (tp + tn + fp + fn);
        float f1score   = (precision + recall) == 0 ? 0f : (2f * precision * recall) / (precision + recall);

        map.put("precision", precision);
        map.put("recall", recall);
        map.put("f1score", f1score);
        map.put("accuracy", accuracy);

        return map;
    }

    public static void generateMetrics(String sums, String metrics, String prom) throws IOException {
        String fileName = "Metrics" + ".csv";

        FileWriter writer = new FileWriter(fileName);
        writer.write(sums + "," + "\n" + metrics + "\n" + prom + "\n");
        writer.close();
    }

    public static String generateClassSum(String clase, HashMap<String, Integer> evaluations){
        String result = clase + ")" + " TP=" + evaluations.get("tp") + " FP=" + evaluations.get("fp") + " TN=" +
                evaluations.get("tn")+ " FN=" + evaluations.get("fn") + " ";
        return result;
    }


    public static void readData(String filename, int classNumber) throws IOException {
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        HashSet<String> clases = new HashSet<>();

        String line;

        int rows = 0;
        while((line = bufferedReader.readLine()) != null) {
            if(line.contains("ID,ClaseVerdadera") ){
                continue;
            }
            rows++;
            String[] data = line.split(",");
            String real = data[1].trim();
            String pred = data[2].trim();

            clases.add(real);
            clases.add(pred);
        }

        if(clases.size() != classNumber){
            System.out.println("La cantidad de clases no coincide con la realidad.");
            return;
        }

        String[][] matrix = new String[rows][2];
        StringBuilder sumatorias = new StringBuilder();
        StringBuilder metricasClase = new StringBuilder();

        float precision = 0;
        float recall = 0;
        float accuracy = 0;
        float f1score = 0;

        for(String clase : clases){
            matrix = getEvaluation(clase, filename, rows, 2);

            generateCSVevaluation(clase, matrix);
            HashMap<String, Integer> evaluations = generateClassEvaluations(clase, matrix);

            sumatorias.append(generateClassSum(clase, evaluations));

            HashMap<String, Float> classMetrics = generateClassMetrics(evaluations);

            metricasClase.append(clase).append(") ");
            for(String key : classMetrics.keySet()){
                metricasClase.append(key).append("=").append(classMetrics.get(key)).append(" , ");
            }

            precision += classMetrics.get("precision");
            recall += classMetrics.get("recall");
            accuracy += classMetrics.get("accuracy");
            f1score += classMetrics.get("f1score");



        }

        precision = precision / classNumber;
        recall = recall / classNumber;
        accuracy = accuracy / classNumber;
        f1score = f1score / classNumber;

        StringBuilder prom = new StringBuilder();
        prom.append("Precision=").append(precision).append(",Accuracy=").append(accuracy).append(",Recall=").append(recall).append(",F1-score=").append(f1score).append("\n");
        generateMetrics(sumatorias.toString(), metricasClase.toString(), prom.toString());
        generateConfusionMatrix(filename, clases);


        System.out.println(clases.toString());
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        System.out.println("Ingresa el nombre de tu archivo: ");
        String fileName = sc.nextLine();
        System.out.println("Ingresa la cantidad de clases que tiene el archivo: ");
        int n = sc.nextInt();
        readData(fileName, n);

    }
}