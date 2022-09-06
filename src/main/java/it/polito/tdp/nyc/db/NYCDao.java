package it.polito.tdp.nyc.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.nyc.model.Adiacenza;
import it.polito.tdp.nyc.model.Hotspot;

public class NYCDao {

	public List<Hotspot> getAllHotspot() {
		String sql = "SELECT * FROM nyc_wifi_hotspot_locations";
		List<Hotspot> result = new ArrayList<>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Hotspot(res.getInt("OBJECTID"), res.getString("Borough"), res.getString("Type"),
						res.getString("Provider"), res.getString("Name"), res.getString("Location"),
						res.getDouble("Latitude"), res.getDouble("Longitude"), res.getString("Location_T"),
						res.getString("City"), res.getString("SSID"), res.getString("SourceID"), res.getInt("BoroCode"),
						res.getString("BoroName"), res.getString("NTACode"), res.getString("NTAName"),
						res.getInt("Postcode")));
			}

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Error");
		}

		return result;
	}
	
	public List<Hotspot> getAllHotspotWithProviderAndQuartiere(String provider, String quartiere) {
		String sql = "SELECT * FROM nyc_wifi_hotspot_locations WHERE Provider = ? AND City = ?";
		List<Hotspot> result = new ArrayList<>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, provider);
			st.setString(2, quartiere);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(new Hotspot(res.getInt("OBJECTID"), res.getString("Borough"), res.getString("Type"),
						res.getString("Provider"), res.getString("Name"), res.getString("Location"),
						res.getDouble("Latitude"), res.getDouble("Longitude"), res.getString("Location_T"),
						res.getString("City"), res.getString("SSID"), res.getString("SourceID"), res.getInt("BoroCode"),
						res.getString("BoroName"), res.getString("NTACode"), res.getString("NTAName"),
						res.getInt("Postcode")));
			}

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Error");
		}

		return result;
	}

	public List<String> getAllProvider() {
		String sql = "SELECT DISTINCT Provider " + "FROM nyc_wifi_hotspot_locations " + "ORDER BY Provider";
		List<String> result = new ArrayList<>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(res.getString("Provider"));
			}

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Error");
		}

		return result;
	}

	public List<String> getAllVertici(String provider) {
		String sql = "SELECT DISTINCT City " + "FROM nyc_wifi_hotspot_locations " + "WHERE Provider = ?";
		List<String> result = new ArrayList<>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, provider);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				result.add(res.getString("City"));
			}

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Error");
		}

		return result;
	}

	public List<Adiacenza> getAllAdiacenze(String provider) {
		String sql = "SELECT t1.City AS c1, t2.City AS c2 "
				+ "FROM nyc_wifi_hotspot_locations t1, nyc_wifi_hotspot_locations t2 "
				+ "WHERE t1.Provider = t2.Provider AND t1.Provider = ? AND t1.City > t2.City "
				+ "GROUP BY t1.City, t2.City";
		List<Adiacenza> result = new ArrayList<>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, provider);
			ResultSet res = st.executeQuery();

			while (res.next()) {

				String citta1 = res.getString("c1");
				String citta2 = res.getString("c2");

				LatLng posizione1 = this.getPosizione(provider, citta1);
				LatLng posizione2 = this.getPosizione(provider, citta2);

				result.add(new Adiacenza(citta1, citta2,
						LatLngTool.distance(posizione1, posizione2, LengthUnit.KILOMETER)));

			}

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Error");
		}

		return result;
	}

	public LatLng getPosizione(String provider, String citta) {
		String sql = "SELECT City, AVG(Latitude) as avg_lat, AVG(Longitude) as avg_lon "
				+ "FROM nyc_wifi_hotspot_locations " + "WHERE Provider = ? AND City = ?";
		LatLng posizione = null;
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, provider);
			st.setString(2, citta);
			ResultSet res = st.executeQuery();

			res.first();

			double lat = res.getDouble("avg_lat");
			double lng = res.getDouble("avg_lon");
			posizione = new LatLng(lat, lng);

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Error");
		}
		return posizione;
	}

}
