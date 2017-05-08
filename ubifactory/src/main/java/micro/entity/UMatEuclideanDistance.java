package micro.entity;

import somlp.model.nn.ubisom.Prototype;

import java.util.ArrayList;
import java.util.List;

public class UMatEuclideanDistance extends UMatDistanceCalculator{

    public UMatEuclideanDistance(Prototype[][] prototypes) {
        super(UMatDistanceCalculatorType.EUCLIDEAN, prototypes);
    }

    @Override
    public double getDistance(int protoX, int protoY, int radius) {
        List<Double> distances = new ArrayList<>();
        int MIN_X = 0;
        int MAX_X = this.prototypes.length - 1;
        int MIN_Y = 0;
        int MAX_Y = this.prototypes[0].length - 1;
        int startPosX = (protoX - radius < MIN_X) ? protoX - (protoX - MIN_X) : protoX-radius;
        int startPosY = (protoY - radius < MIN_Y) ? protoY - (protoY - MIN_Y) : protoY-radius;
        int endPosX =   (protoX + radius > MAX_X) ? protoX + (MAX_X - protoX) : protoX+radius;
        int endPosY =   (protoY + radius > MAX_Y) ? protoY + (MAX_Y - protoY) : protoY+radius;

        for (int rowNum=startPosX; rowNum<=endPosX; rowNum++)
            for (int colNum = startPosY; colNum <= endPosY; colNum++) {
                // All the neighbors will be grid[rowNum][colNum]
                if(rowNum == startPosX && colNum == startPosY)
                    continue;
                if(rowNum == endPosX && colNum == startPosY)
                    continue;
                if(rowNum == startPosX && colNum == endPosY)
                    continue;
                if(rowNum == endPosX && colNum == endPosY)
                    continue;

                if (rowNum == protoX && colNum == protoY)
                    continue;
                double distance = this.prototypes[protoX][protoY]
                        .distanceEuclidean(this.prototypes[rowNum][colNum]);
                distances.add(distance);
            }

        return distances.stream()
                .reduce(0.0, Double::sum) / distances.size();
    }

}
