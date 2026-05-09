package model;

/**
 * DTO pour les données météo actuelles (Open-Meteo).
 */
public class WeatherInfo {

    private double temperature;
    private int weatherCode;
    private String description;
    private Integer humidity;

    public WeatherInfo() {
    }

    public WeatherInfo(double temperature, int weatherCode, String description, Integer humidity) {
        this.temperature = temperature;
        this.weatherCode = weatherCode;
        this.description = description;
        this.humidity = humidity;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }
}
