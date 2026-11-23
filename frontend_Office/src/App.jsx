import { BrowserRouter, Routes, Route } from "react-router-dom";
import Office from "./components/Office.jsx";
import OfficeList from "./components/OfficeList.jsx";

function App() {

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Office />} />
                <Route path="/list" element={<OfficeList />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App