import { useState } from "react";

function HomePage() {
  const [isCreateAccountButtonClicked, setIsCreateAccountButtonClicked] = useState(false);

  const books = [
    {
      code: "FSD001",
      name: "Full Stack Development",
      price: 100,
    },
    {
      code: "SOD001",
      name: "Software Engineering",
      price: 80,
    },
    {
      code: "FIN001",
      name: "Finance management",
      price: 90,
    },
    {
      code: "ACC001",
      name: "Accounting",
      price: 30,
    },
  ];

  return (
    <div>
      <ul>
        {books.map((book) => (
          <li key={book.code}>
            This is the name {book.name} and its price is {book.price}
          </li>
        ))}
      </ul>

      <div>
        {isCreateAccountButtonClicked ? (
          <div>Create account</div>
        ) : (
          <div>Login into Home</div>
        )}
      </div>

      <div>
        The total price for {books.length} books is:{" "}
        {books.reduce((total, book) => total + book.price, 0)}
      </div>
    </div>
  );
}

export default HomePage;