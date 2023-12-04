import "./NavigationBar.css"

export const NavigationBar: React.FC = () => {
   return (
      <div className="NavigationBar-container">
         <a href="/" className="NavigationBar-nav-logo-anchor"><div className="NavigationBar-nav-item NavigationBar-nav-logo">
            <img src="/logo.png" />
            <p>BibleGuessr</p>
         </div></a>
         <div className="NavigationBar-nav-item">
                {/* <h1>Home</h1> */}
            </div>
         {/* <div className="NavigationBar-nav-item">
                <h1>Account</h1>
            </div>
            <div className="NavigationBar-nav-item">
                <h1>News</h1>
            </div>
            <div className="NavigationBar-nav-item">
                <h1>Sign Up</h1>
            </div>
            <div className="NavigationBar-nav-item">
                <h1>Log in</h1>
            </div> */}
      </div>
   )
}