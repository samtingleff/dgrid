package com.dgrid.test.helpers;

import java.util.ArrayList;
import java.util.List;

import com.dgrid.helpers.SDBHelper;
import com.dgrid.test.BaseTestCase;
import com.xerox.amazonws.sdb.Domain;
import com.xerox.amazonws.sdb.Item;
import com.xerox.amazonws.sdb.ItemAttribute;
import com.xerox.amazonws.sdb.QueryResult;
import com.xerox.amazonws.sdb.SimpleDB;

public class SimpleDBTestCase extends BaseTestCase
{
	/*
	public void testUserArchive() throws Exception {
		String domainName = "users";
		SDBHelper sdbHelper = (SDBHelper) super.getBean(SDBHelper.NAME);
		SimpleDB sdb = sdbHelper.getSimpleDB();
		Domain domain = sdb.createDomain(domainName);
		// list all items
		QueryResult allItems = domain.listItems();
		List<Item> all = allItems.getItemList();
		for (Item item2 : all)
		{
			System.out.println("id: " + item2.getIdentifier());
			List<ItemAttribute> attributes = item2.getAttributes();
			for (ItemAttribute itemAttribute : attributes)
			{
				System.out.println("list result:");
				System.err.println(itemAttribute.getName() + "=" + itemAttribute.getValue());
			}
			domain.deleteItem(item2.getIdentifier());
		}
		// search
		QueryResult value6Result = domain.listItems("['storage' = 'mdbv1'] sort 'storage'");
		List<Item> value6ResultItems = value6Result.getItemList();
		for (Item item2 : value6ResultItems)
		{
			System.out.println("search result:");
			System.out.println("id: " + item2.getIdentifier());
		}
	}
	*/
	
	public void testSimpleDB() throws Exception
	{
		String domainName = "users";
		SDBHelper sdbHelper = (SDBHelper) super.getBean(SDBHelper.NAME);
		SimpleDB sdb = sdbHelper.getSimpleDB();
		Domain domain = sdb.createDomain(domainName);
/*
		Item item = domain.getItem("56654977");
		List<ItemAttribute> list = new ArrayList<ItemAttribute>();
		list.add(new ItemAttribute("storage", "mdbv1", false));
		list.add(new ItemAttribute("partner", "4049", false));
		item.putAttributes(list);
*/
		// search
		QueryResult value6Result = domain.listItems("['storage' = 'mdbv1'] sort 'storage'");
		List<Item> value6ResultItems = value6Result.getItemList();
		for (Item item2 : value6ResultItems)
		{
			System.err.println("id: " + item2.getIdentifier());
			for (ItemAttribute itemAttribute : item2.getAttributes())
			{
				System.err.println(" " + itemAttribute.getName() + "=" + itemAttribute.getValue());
			}
		}
	}

	/*
	public void testSimpleDB() throws Exception
	{
		String domainName = "test.domain";
		SDBHelper sdbHelper = (SDBHelper) super.getBean(SDBHelper.NAME);
		SimpleDB sdb = sdbHelper.getSimpleDB();
		Domain domain = sdb.createDomain(domainName);

		Item item = domain.getItem("test.item");
		List<ItemAttribute> list = new ArrayList<ItemAttribute>();
		list.add(new ItemAttribute("test1", "value1", false));
		list.add(new ItemAttribute("test1", "value5", false));
		list.add(new ItemAttribute("test1", "value6", false));
		list.add(new ItemAttribute("test1", "value7", false));
		list.add(new ItemAttribute("test1", "value8", false));
		list.add(new ItemAttribute("test2", "value9", false));
		list.add(new ItemAttribute("test2", "value10", false));
		list.add(new ItemAttribute("test2", "value11", false));
		list.add(new ItemAttribute("test2", "value12", false));
		item.putAttributes(list);

		// list all items
		QueryResult allItems = domain.listItems();
		List<Item> all = allItems.getItemList();
		for (Item item2 : all)
		{
			System.out.println("id: " + item2.getIdentifier());
		}
		// search
		QueryResult value6Result = domain.listItems("['test1' = 'value6'] sort 'test1'");
		List<Item> value6ResultItems = value6Result.getItemList();
		for (Item item2 : value6ResultItems)
		{
			System.out.println("id: " + item2.getIdentifier());
		}
	}
	*/
}
