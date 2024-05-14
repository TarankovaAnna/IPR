import animals.*;
import employee.Worker;
import food.Grass;
import food.Meat;

public class Zoo {
    public static Swim[] createPond() {
        Swim[] pondAnimals = {new Duck(), new Fish(), new Walrus()};
        return pondAnimals;
    }

    public static void main(String[] args) {
        Kotik cat = new Kotik();
        Bear bear = new Bear();
        Cow cow = new Cow();
        Duck duck = new Duck();
        Fish fish = new Fish();
        Walrus walrus = new Walrus();
        Worker worker = new Worker();
        Meat meat = new Meat();
        Grass grass = new Grass();

        Swim[] pond = createPond();

        for (Swim animal : pond) {
            animal.swim();
        }

        System.out.println(" ");
        worker.feed(duck, meat);
        worker.feed(duck,grass);
        System.out.println(" ");
        worker.feed(cat, meat);
        worker.feed(cat, grass);
        System.out.println(" ");
        worker.feed(walrus, meat);
        worker.feed(walrus, grass);
        System.out.println(" ");
        worker.feed(bear, meat);
        worker.feed(bear, grass);
        System.out.println(" ");
        worker.feed(fish, meat);
        worker.feed(fish, grass);
        System.out.println(" ");
        worker.feed(cow, meat);
        worker.feed(cow, grass);
        System.out.println(" ");
        worker.getVoice(cat);
        System.out.println(" ");
        worker.getVoice(walrus);
        System.out.println(" ");
        worker.getVoice(cow);
        System.out.println(" ");
        worker.getVoice(bear);
        System.out.println(" ");
        worker.getVoice(duck);


    }
}
