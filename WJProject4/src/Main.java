import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import org.apache.commons.math3.ml.clustering.*;
import org.apache.commons.math3.ml.distance.*;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;

public class Main {

    static class DataPoint implements Clusterable {
        double[] features;
        int clusterId = -1;

        public DataPoint(double[] features) {
            this.features = features;
        }

        @Override
        public double[] getPoint() {
            return features;
        }
    }

    // 1. 霍普金斯统计量H检测聚类趋势
    public static double calculateHopkinsStatistic(List<DataPoint> dataPoints, int sampleSize) {
        RandomDataGenerator random = new RandomDataGenerator();
        int dimension = dataPoints.get(0).features.length;
        DescriptiveStatistics stats = new DescriptiveStatistics();

        // 计算原始数据点与最近邻的距离
        for (int i = 0; i < sampleSize; i++) {
            DataPoint p = dataPoints.get(random.nextInt(0, dataPoints.size() - 1));
            double minDist = Double.MAX_VALUE;

            for (DataPoint q : dataPoints) {
                if (p != q) {
                    double dist = new EuclideanDistance().compute(p.features, q.features);
                    if (dist < minDist) minDist = dist;
                }
            }
            stats.addValue(minDist);
        }
        double w = stats.getSum();

        // 生成均匀随机点并计算距离
        stats.clear();
        double[] mins = new double[dimension];
        double[] maxs = new double[dimension];
        Arrays.fill(mins, Double.MAX_VALUE);
        Arrays.fill(maxs, Double.MIN_VALUE);

        for (DataPoint p : dataPoints) {
            for (int i = 0; i < dimension; i++) {
                mins[i] = Math.min(mins[i], p.features[i]);
                maxs[i] = Math.max(maxs[i], p.features[i]);
            }
        }

        for (int i = 0; i < sampleSize; i++) {
            double[] randomPoint = new double[dimension];
            for (int j = 0; j < dimension; j++) {
                randomPoint[j] = random.nextUniform(mins[j], maxs[j]);
            }

            double minDist = Double.MAX_VALUE;
            for (DataPoint q : dataPoints) {
                double dist = new EuclideanDistance().compute(randomPoint, q.features);
                if (dist < minDist) minDist = dist;
            }
            stats.addValue(minDist);
        }
        double u = stats.getSum();

        return u / (u + w);
    }

    // 2. 手肘法确定最佳聚类数量
    public static void elbowMethod(List<DataPoint> dataPoints, int maxK) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        double[][] values = new double[2][maxK];

        for (int k = 1; k <= maxK; k++) {
            KMeansPlusPlusClusterer<DataPoint> clusterer =
                    new KMeansPlusPlusClusterer<>(k, 100, new EuclideanDistance());
            List<CentroidCluster<DataPoint>> clusters = clusterer.cluster(dataPoints);

            double wssse = 0.0;
            for (CentroidCluster<DataPoint> cluster : clusters) {
                double[] centroid = cluster.getCenter().getPoint();
                for (DataPoint point : cluster.getPoints()) {
                    wssse += Math.pow(new EuclideanDistance().compute(point.getPoint(), centroid), 2);
                }
            }

            values[0][k-1] = k;
            values[1][k-1] = wssse;
        }

        dataset.addSeries("WSSSE", values);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Elbow Method for Optimal K",
                "Number of Clusters (k)",
                "Within-Cluster Sum of Squares (WSSSE)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartFrame frame = new ChartFrame("Elbow Method", chart);
        frame.pack();
        frame.setVisible(true);
    }

    // 3. 使用K-Means进行聚类
    public static List<DataPoint> performKMeansClustering(List<DataPoint> dataPoints, int k) {
        KMeansPlusPlusClusterer<DataPoint> clusterer =
                new KMeansPlusPlusClusterer<>(k, 100, new EuclideanDistance());
        List<CentroidCluster<DataPoint>> clusters = clusterer.cluster(dataPoints);

        // 为每个点分配簇ID
        int clusterId = 0;
        for (CentroidCluster<DataPoint> cluster : clusters) {
            for (DataPoint point : cluster.getPoints()) {
                point.clusterId = clusterId;
            }
            clusterId++;
        }

        return dataPoints;
    }

    // 4. 计算轮廓系数
    public static double calculateSilhouetteScore(List<DataPoint> dataPoints) {
        double total = 0.0;
        int n = dataPoints.size();

        for (DataPoint p : dataPoints) {
            // 计算a(o_i): p与同簇其他点的平均距离
            double a = 0.0;
            int aCount = 0;

            // 计算b(o_i): p与最近其他簇的平均距离
            Map<Integer, Double> clusterDistances = new HashMap<>();
            Map<Integer, Integer> clusterCounts = new HashMap<>();

            for (DataPoint q : dataPoints) {
                if (p != q) {
                    double dist = new EuclideanDistance().compute(p.features, q.features);
                    if (q.clusterId == p.clusterId) {
                        a += dist;
                        aCount++;
                    } else {
                        clusterDistances.merge(q.clusterId, dist, Double::sum);
                        clusterCounts.merge(q.clusterId, 1, Integer::sum);
                    }
                }
            }

            a = (aCount > 0) ? a / aCount : 0;

            double b = Double.MAX_VALUE;
            for (Map.Entry<Integer, Double> entry : clusterDistances.entrySet()) {
                int cluster = entry.getKey();
                double avgDist = entry.getValue() / clusterCounts.get(cluster);
                if (avgDist < b) b = avgDist;
            }

            double s = (aCount > 0) ? (b - a) / Math.max(a, b) : 0;
            total += s;
        }

        return total / n;
    }

    // 加载数据
    public static List<DataPoint> loadData(String filename) throws IOException {
        List<DataPoint> dataPoints = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        // 跳过标题行
        reader.readLine();

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 5) { // 假设前4列是特征，第5列可能是标签
                double[] features = new double[4];
                for (int i = 0; i < 4; i++) {
                    features[i] = Double.parseDouble(parts[i]);
                }
                dataPoints.add(new DataPoint(features));
            }
        }

        reader.close();
        return dataPoints;
    }

    public static void main(String[] args) {
        try {
            // 加载数据
            List<DataPoint> dataPoints = loadData("clusters_4d_data.csv");
            System.out.println("数据加载完成，共 " + dataPoints.size() + " 个数据点");

            // 1. 计算霍普金斯统计量
            double hopkinsStat = calculateHopkinsStatistic(dataPoints, Math.min(100, dataPoints.size()));
            System.out.println("\n1. 霍普金斯统计量 H = " + hopkinsStat);
            System.out.println("   H > 0.75: 强聚类趋势");
            System.out.println("   0.5 < H ≤ 0.75: 一般聚类趋势");
            System.out.println("   H ≤ 0.5: 无显著聚类趋势");

            // 2. 手肘法确定最佳K值
            System.out.println("\n2. 运行手肘法确定最佳聚类数量...");
            elbowMethod(dataPoints, 10);
            System.out.println("   请查看弹出的图表，选择WSSSE下降变缓的拐点作为最佳K值");

            // 假设我们根据手肘法选择k=3
            int optimalK = 3;
            System.out.println("   根据手肘法，选择 k = " + optimalK);

            // 3. 执行聚类
            System.out.println("\n3. 执行K-Means聚类 (k=" + optimalK + ")...");
            List<DataPoint> clusteredData = performKMeansClustering(dataPoints, optimalK);
            System.out.println("   聚类完成");

            // 4. 计算轮廓系数
            System.out.println("\n4. 计算轮廓系数...");
            double silhouetteScore = calculateSilhouetteScore(clusteredData);
            System.out.println("   整体轮廓系数 S = " + silhouetteScore);
            System.out.println("   S ≈ 1: 聚类结果优秀");
            System.out.println("   S ≈ 0: 聚类重叠");
            System.out.println("   S ≈ -1: 聚类结果差");

        } catch (IOException e) {
            System.err.println("加载数据错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}