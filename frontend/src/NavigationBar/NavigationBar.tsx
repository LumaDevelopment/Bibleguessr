import "./NavigationBar.css"

export const NavigationBar: React.FC = () => {
    return (
        <div className="NavigationBar-container">
            <div className="NavigationBar-nav-item">
                <h1>Logo</h1>
            </div>
            <div className="NavigationBar-nav-item">
                <h1>Play</h1>
            </div>
            <div className="NavigationBar-nav-item">
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
            </div>
        </div>
    )
}