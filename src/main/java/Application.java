import animals.Kotik;

public class Application {
    public static boolean compareVoice(Kotik one, Kotik two) {
        if (one.getVoice().equals(two.getVoice())) {

        } else {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Kotik kat1 = new Kotik("Барсик", "Мау", 5, 5);
        Kotik kat2 = new Kotik();
        kat2.setName("Мурзик");
        kat2.setVoice("Миу");
        kat2.setSatiety(20);
        kat2.setWeight(9);


        System.out.println(kat1.liveAnotherDay());


        System.out.println(" ");

        System.out.println("Котик: " + kat1.getName() + ", вес: " + kat1.getWeight() + "кг.");
        System.out.println(" ");

        if (Application.compareVoice(kat1, kat2) == true) {
            System.out.println("Котики разговаривают одинаково.");
        } else {
            System.out.println("Котики разговаривают по-разному.");
        }
        System.out.println(" ");

        System.out.println("Всего создано котиков: " + Kotik.getCount() + ".");

    }

}