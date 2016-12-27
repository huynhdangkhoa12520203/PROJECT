package entities;

import java.util.ArrayList;

/**
 * Created by MinhNhan on 28/05/2016.
 */
public class AdressDetail {
    private ArrayList<String> cityList = new ArrayList<>();

    public String getCurrentCity() {
        return currentCity;
    }


    private String currentCity;
    private Adress Hcm;


    public AdressDetail(String name) {
        this.currentCity = new String("TP. Hồ Chí Minh");
        if (name.equals("Hcm")) {
            getCityList().add("TP. Hồ Chí Minh");
            setHcm();
        }

    }

    public void setHcm() {
        Hcm = new Adress();
        Hcm.setName("TP. Hồ Chí Minh");
        Hcm.setDist("Quận 1");
        Hcm.setDist("Quận 2");
        Hcm.setDist("Quận 3");
        Hcm.setDist("Quận 4");
        Hcm.setDist("Quận 5");
        Hcm.setDist("Quận 6");
        Hcm.setDist("Quận 7");
        Hcm.setDist("Quận 8");
        Hcm.setDist("Quận 9");
        Hcm.setDist("Quận 10");
        Hcm.setDist("Quận 11");
        Hcm.setDist("Quận 12");
        Hcm.setDist("Quận Thủ Đức");
        Hcm.setDist("Quận Gò Vấp");
        Hcm.setDist("Quận Bình Thạnh");
        Hcm.setDist("Quận Tân Bình");
        Hcm.setDist("Quận Tân Phú");
        Hcm.setDist("Quận Phú Nhuận");
        Hcm.setDist("Quận Bình Tân");
        Hcm.setDist("Huyện Củ Chi");
        Hcm.setDist("Huyện Hóc Môn");
        Hcm.setDist("Huyện Bình Chánh");
        Hcm.setDist("Huyện Nhà Bè");
        Hcm.setDist("Huyện Cần Giờ");
    }
    public ArrayList<String> getCityList() {
        return cityList;
    }


}
