package com.example.sportcenterapp.utils;

import com.example.sportcenterapp.models.Product;
import java.util.*;

public class CartStore {
    public static class Item { public Product p; public int qty; public Item(Product p,int q){this.p=p;this.qty=q;} }
    private static final Map<Integer, Item> map = new LinkedHashMap<>();

    public static void add(Product p){
        Item it = map.get(p.id);
        if (it == null) map.put(p.id, new Item(p,1));
        else it.qty++;
    }
    public static List<Item> all(){ return new ArrayList<>(map.values()); }
    public static void clear(){ map.clear(); }
    public static int totalCount(){ int c=0; for (Item i: map.values()) c+=i.qty; return c; }
}
