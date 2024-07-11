import axios from "axios";

const headers = {
    Accept: "application/json",
    "Content-Type": "application/json",
  };

export default axios.create({
    baseURL: "http://localhost:8080",
    headers
})