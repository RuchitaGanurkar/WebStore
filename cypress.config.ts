import { defineConfig } from "cypress";
import { Client } from 'pg';

export default defineConfig({
  e2e: {
    setupNodeEvents(on, config) {
      // implement node event listeners here
      on("task", {
        async connectDB(){
          const client = new Client({
            user: "postgres",
            password: "postgres",
            host: "localhost",
            database: "postgres",
            ssl: false,
            port: 5432
          })
          await client.connect();
          const res = await client.query('SELECT currency_code FROM web_store.currency WHERE currency_id = 1;');
          await client.end();
          return res.rows;
        }
      });
    }
  },
});
