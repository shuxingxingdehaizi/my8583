package org.ethan.my8583;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.etyhan.my8583.util.StringUtil;

public class BitMap {
	
	private byte[] map;
	
	
	public BitMap() {}
	
	public BitMap(byte[] bitMapByte) {
		this.map = bitMapByte;
	}
	
	public BitMap(List<Integer>fieldIndexList) {
		
		fieldIndexList.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				// TODO Auto-generated method stub
				return o1 - o2;
			}
		});
		
		if(fieldIndexList.get(fieldIndexList.size()-1) <= 64) {
			map = new byte[8];
		}else {
			map = new byte[16];
		}
		
//		 byte[] temp = new byte[len];
         int t1, t2;
         byte t3;
         for (int i = 0; i < map.length; i++)
         {
        	 map[i] = 0x00;
         }
         for (int k = 0; k < fieldIndexList.size(); k++)
         {
             if (fieldIndexList.get(k) > 8 * map.length || fieldIndexList.get(k) <= 0)
             {
                 continue;
             }
             t1 = fieldIndexList.get(k) / 8;
             t2 = fieldIndexList.get(k) % 8;
             if (t2 == 0)
             {
                 t3 = 0x01;
                 map[t1 - 1] |= t3;
             }
             else
             {
                 t3 = (byte) (1 << (8-t2));
                 map[t1] |= t3;
             }
         }
	}
	
	
	public static void main(String[] args) {
		System.out.println(Long.toBinaryString(-1));
		System.out.println(Long.toBinaryString(1<<62));
		BitMap m = new BitMap(new ArrayList<Integer>(Arrays.asList(new Integer[] {2,5,10,64,128})));
		System.out.println(StringUtil.toBinaryStr8(m.map));
	}
}
