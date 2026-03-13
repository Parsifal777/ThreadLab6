package firstzadanie;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataGenerator {

    public static void generateBinaryFile(String filename, int count) throws IOException {
        Random random = new Random();

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename))) {
            for (int i = 0; i < count; i++) {
                // Генерируем случайные вещественные числа от 0 до 100
                double value = random.nextDouble() * 100;
                dos.writeDouble(value);
            }
        }

        System.out.println("Сгенерировано " + count + " чисел в файл " + filename);
    }

    public static double[] readBinaryFile(String filename) throws IOException {
        List<Double> numbersList = new ArrayList<>();

        try (DataInputStream dis = new DataInputStream(new FileInputStream(filename))) {
            while (dis.available() > 0) {
                numbersList.add(dis.readDouble());
            }
        }

        double[] numbers = new double[numbersList.size()];
        for (int i = 0; i < numbersList.size(); i++) {
            numbers[i] = numbersList.get(i);
        }

        return numbers;
    }
}
