package app.GameObjects;

import java.util.ArrayList;
import java.util.List;

public class Path {
    private double payload;

    private final List<Integer> route;
    public Path(){
        this.route = new ArrayList<>();
        this.payload = 0;
    }
    public Path(Path path){
        this.route = new ArrayList<>();
        this.payload = path.payload;
        this.route.addAll(path.getRoute());
    }
    public Path(List<Integer> route, int station){
        this.route = new ArrayList<>();
        for(int i = 0; i < route.size(); i++){
            if(route.get(i) == station){
                this.route.add(route.get(i));
                break;
            }
            this.route.add(route.get(i));
        }
    }
    public boolean hasStation(int index){
        return route.contains(index);
    }
    public int routeLength(){
        return route.size();
    }
    public void addStation(int index){
        this.route.add(index);
    }

    public List<Integer> getRoute() {
        return route;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        for(Integer i : route){
            string.append(Integer.toString(i));
        }
        return string.toString();
    }

    public void addPayload(double payload){
        this.payload += payload;
    }
    public double getPayload(){
        return this.payload;
    }
}
