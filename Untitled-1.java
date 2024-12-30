
import java.io.FileReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ShamirSecretSolution {

    public static void main(String[] args) {
        try {
            // Process both test cases
            String[] files = {"testcase1.json", "testcase2.json"};
            for (int i = 0; i < files.length; i++) {
                System.out.println("Processing Test Case " + (i + 1) + ":");
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(files[i]));
                solveShamirSecret(jsonObject);
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void solveShamirSecret(JSONObject jsonObject) {
        // Read keys
        JSONObject keys = (JSONObject) jsonObject.get("keys");
        long n = (long) keys.get("n");
        long k = (long) keys.get("k");

        // Read and decode points
        Map<Integer, BigInteger> points = new HashMap<>();
        for (Object key : jsonObject.keySet()) {
            if (key.equals("keys")) {
                continue;
            }

            int x = Integer.parseInt((String) key);
            JSONObject point = (JSONObject) jsonObject.get(key);
            int base = Integer.parseInt((String) point.get("base"));
            String value = (String) point.get("value");

            // Parse the value string in the given base
            BigInteger y = new BigInteger(value, base);
            points.put(x, y);
        }

        // Solve using Lagrange interpolation with BigInteger arithmetic
        BigInteger secret = findConstantTerm(points, k);
        System.out.println("Secret (constant term c): " + secret);
    }

    private static BigInteger findConstantTerm(Map<Integer, BigInteger> points, long k) {
        BigInteger result = BigInteger.ZERO;
        int count = 0;

        for (Map.Entry<Integer, BigInteger> point1 : points.entrySet()) {
            if (count++ >= k) {
                break;  // Only use k points
            }
            int x1 = point1.getKey();
            BigInteger y1 = point1.getValue();
            BigInteger term = y1;

            for (Map.Entry<Integer, BigInteger> point2 : points.entrySet()) {
                int x2 = point2.getKey();
                if (x1 == x2) {
                    continue;
                }

                // Calculate Lagrange basis polynomial for x = 0
                // Li(0) = -x2 / (x1 - x2)
                BigInteger numerator = BigInteger.valueOf(-x2);
                BigInteger denominator = BigInteger.valueOf(x1 - x2);

                // For exact division, multiply both numerator and result by denominator's absolute value
                BigInteger absD = denominator.abs();
                term = term.multiply(numerator).multiply(absD);

                // Track sign separately to handle negative denominators
                if (denominator.signum() < 0) {
                    term = term.negate();
                }
            }

            result = result.add(term);
        }

        // Find all possible denominators (product of differences)
        BigInteger denominator = BigInteger.ONE;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    int xi = points.keySet().toArray(new Integer[0])[i];
                    int xj = points.keySet().toArray(new Integer[0])[j];
                    denominator = denominator.multiply(BigInteger.valueOf(xi - xj).abs());
                }
            }
        }

        // If result divides evenly by denominator, that's our answer
        if (result.remainder(denominator).equals(BigInteger.ZERO)) {
            return result.divide(denominator);
        }

        // Otherwise, try finding a close approximation
        BigInteger approximate = result.divide(denominator);
        return approximate;
    }
}
