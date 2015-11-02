package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static void insertBeer(Connection conn, Beer beer) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO beers VALUES (?, ?)");
        stmt.setString(1, beer.name);
        stmt.setString(2, beer.type);
        stmt.execute();
    }

    static void deleteBeer(Connection conn, int idNum) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM beers WHERE ROWNUM = ? ");
        stmt.setInt(1, idNum);
        stmt.execute();
    }

    static ArrayList<Beer> selectBeers(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM beers");
        ArrayList<Beer> beers = new ArrayList();
        int id = 1;
        while (results.next()){
            String name = results.getString("name");
            String type = results.getString("type");
            Beer item = new Beer(name, type);
            item.id = id;
            beers.add(item);
            id++;

        }
        return beers;
    }

    static void editBeer(Connection conn, int idNum, String name, String type) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE beers SET name = ? AND type = ? WHERE ROWNUM = ?");
        stmt.setString(1, name);
        stmt.setString(2, type);
        stmt.setInt(3, idNum);
        stmt.execute();
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS beers (name VARCHAR, type VARCHAR)");


        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }
                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("beers", selectBeers(conn));
                    return new ModelAndView(m, "logged-in.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    String username = request.queryParams("username");
                    Session session = request.session();
                    session.attribute("username", username);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-beer",
                ((request, response) -> {
                    Beer beer = new Beer();
                    beer.name = request.queryParams("beername");
                    beer.type = request.queryParams("beertype");
                    insertBeer(conn, beer);
                    //beers.add(beer);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/delete-beer",
                ((request, response) -> {
                    String id = request.queryParams("beerid");
                    try {
                        int idNum = Integer.valueOf(id);
                        deleteBeer(conn, idNum);
                    } catch (Exception e) {

                    }
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/edit-beer",
                ((request, response) -> {
                    String id = request.queryParams("beerid");
                    String name = request.queryParams("beername");
                    String type = request.queryParams("beertype");
                    //String edit = request.queryParams("edit-beer");
                    try {
                        int idNum = Integer.valueOf(id);
                        editBeer(conn, idNum, name, type);

                    } catch (Exception e){

                    }
                    response.redirect("/");
                    return ("");

                })
        );
    }
}
