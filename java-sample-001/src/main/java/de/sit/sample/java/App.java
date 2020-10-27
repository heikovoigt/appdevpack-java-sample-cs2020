package de.sit.sample.java;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListenableFuture;
import com.hcl.domino.db.model.*;

/**
 * Hello world!
 */

public final class App {
    private App() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        
        final String host = "sitfp10.sit.de";
		final int port = 3002;
		final String clientCert = "/Users/hvoigt/Documents/GitHub/appdevpack-java-sample/java-sample-001/certs/sitfp10/app1.crt";
		final String clientKey = "/Users/hvoigt/Documents/GitHub/appdevpack-java-sample/java-sample-001/certs/sitfp10/app1.key";
		final String trustedRoots = "/Users/hvoigt/Documents/GitHub/appdevpack-java-sample/java-sample-001/certs/sitfp10/ca.crt";
		final String keyPassword = null;
		final String idFilePassword = null;
		final String database = "sittodosample.nsf";
		final String query = "Form = 'ToDoItem'";
		try {
			Server server = new Server(
					host, 
					port, 
					new File(trustedRoots),
					new File(clientCert),
					new File(clientKey), 
					keyPassword, 
					idFilePassword, 
					Executors.newSingleThreadExecutor());
			Database db = server.useDatabase(database);
			List<String> items = new ArrayList<String>();
			items.add("Responsible");
			items.add("Subject");
			
			// Wait for computation to complete and then recieve result
			List<Document> docs = db.readDocuments(query, new OptionalItemNames(items)).get();
			for( Document doc : docs ) {
				List<Item<?>> author = doc.getItemByName("Responsible");
				List<Item<?>> subject = doc.getItemByName("Subject");
				
				System.out.println(author.get(0).getValue().get(0) + " " + subject.get(0).getValue().get(0));
			}

			// Now, let's create a new document via Domino-DB
			List<Item<?>> itemList = new ArrayList<Item<?>>();
    		itemList.add(new TextItem("Form", "ToDoItem"));
    		itemList.add(new TextItem("Subject", "Star Wars is crap"));
    		itemList.add(new TextItem("Responsible", "Disney"));
    		
			ListenableFuture<Document> createResponse = db.createDocument(new Document(itemList));

			// Now, let's call a LotusScript agent that creates new documents
			
			String agentquery = "Form = 'ToDoItem'";
			
			OptionalSelection optSelection = new OptionalSelection(agentquery);
			System.out.println("ID: "+createResponse.get().getUnid());
			OptionalContext optContext = new OptionalContext(createResponse.get().getUnid());
			Agent agent = db.useAgent("(CreateDocuments)");
			ListenableFuture<AgentRunResponse> response = agent.run(optSelection,optContext);

			// Then, let's read the documents one more time:

			List<Document> docs_now = db.readDocuments(query, new OptionalItemNames(items)).get();
			for( Document doc : docs_now ) {
				List<Item<?>> author = doc.getItemByName("Responsible");
				List<Item<?>> subject = doc.getItemByName("Subject");
				System.out.println(author.get(0).getValue().get(0) + " " + subject.get(0).getValue().get(0));
			}

		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (BulkOperationException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		} catch (ExecutionException e) {
			
			e.printStackTrace();
		}



	}
        
}

